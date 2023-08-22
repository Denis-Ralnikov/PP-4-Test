package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WithMockUser(username = "admin", password = "admin", authorities = "ROLE_MODERATOR")
public class TestException extends BaseIntegrationTest {
    @MockBean
    private Keycloak keycloakClient;
    @MockBean
    RealmResource realmResourceMock;
    @MockBean
    UsersResource userResourceMock;
    @MockBean
    Response mockResponse;
    @MockBean
    UserResponse mockUserResponse;
    @MockBean
    UserRepresentation mockUserRepresentation;
    @MockBean
    UserResource mockUserResource;
    @MockBean
    List<RoleRepresentation> mockUserRoles;
    @MockBean
    List<GroupRepresentation> mockUserGroups;
    @MockBean
    RoleMappingResource mockRoleMapping;
    @MockBean
    MappingsRepresentation mockMappingRepresentation;

    @Test
    @SneakyThrows
    void createUserException() {
        UserRequest request = new UserRequest("keks", "keks@mail.ru", "keks12", "keksov", "Lokos");
        when(keycloakClient.realm(anyString())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(userResourceMock);
        when(userResourceMock.create(any(UserRepresentation.class))).thenReturn(Response.serverError().build());
        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
    @Test
    @SneakyThrows
    void getUserByIdException(){
        UUID userId = UUID.randomUUID();

        when(keycloakClient.realm(anyString())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(userResourceMock);
        when(userResourceMock.get(String.valueOf(any(UUID.class)))).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenThrow(RuntimeException.class);

        when(mockUserResource.roles()).thenReturn(mockRoleMapping);
        when(mockRoleMapping.getAll()).thenReturn(mockMappingRepresentation);
        when(mockMappingRepresentation.getRealmMappings()).thenThrow(RuntimeException.class);

        when(mockUserResource.groups()).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,() -> mockUserResource.toRepresentation());
        assertThrows(RuntimeException.class, () -> mockUserResource.roles().getAll().getRealmMappings());
        assertThrows(RuntimeException.class, () -> mockUserResource.groups());


        mvc.perform(requestWithContent(get("/api/users/{id}", userId), userId))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
}
