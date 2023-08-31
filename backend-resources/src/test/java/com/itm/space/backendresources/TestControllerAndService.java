package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WithMockUser(username = "admin", password = "admin", authorities = "ROLE_MODERATOR")
public class TestControllerAndService extends BaseIntegrationTest {
    @MockBean
    Keycloak keycloakClient;
    @Mock
    RealmResource realmResourceMock;
    @Mock
    UsersResource usersResourceMock;
    @Mock
    Response mockResponse;
    @Mock
    UserResource mockUserResource;
    @Mock
    UserRepresentation mockUserRepresentation;
    @Mock
    RoleMappingResource mockRoleMapping;
    @Mock
    MappingsRepresentation mockMappingRepresentation;


    @Test
    public void myTestCreate() throws Exception {
        UserRequest request = new UserRequest("keks", "keks@mail.ru", "keks12", "keksov", "Lokos");

        when(keycloakClient.realm(anyString())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusInfo()).thenReturn(Response.Status.CREATED);

        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById() throws Exception {
        UUID userId = UUID.randomUUID();

        when(keycloakClient.realm(ArgumentMatchers.anyString())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(String.valueOf(userId))).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(mockUserRepresentation);
        when(mockUserResource.roles()).thenReturn(mockRoleMapping);
        when(mockRoleMapping.getAll()).thenReturn(mockMappingRepresentation);

        mvc.perform(get("/api/users/" + userId))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @SneakyThrows
    void createUserExceptionBadRequest() {
        UserRequest request = new UserRequest("keks", "keks@mail.ru", "keks12", "keksov", "Lokos");

        when(keycloakClient.realm(anyString())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.create(any(UserRepresentation.class))).thenThrow(new WebApplicationException("bad request", Response.Status.BAD_REQUEST));

        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @SneakyThrows
    void getUserByIdFailed() throws Exception {
        UUID userId = UUID.randomUUID();

        when(keycloakClient.realm(anyString())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(String.valueOf(any(UUID.class)))).thenReturn(mockUserResource);
        when(mockResponse.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);


        mvc.perform(get("/api/users/" + userId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    public void badUserRequestTest() throws Exception {
        UserRequest request = new UserRequest("e", "keks@mail.ru", "156", "", "");

        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void testHello() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("admin"));
    }

}
