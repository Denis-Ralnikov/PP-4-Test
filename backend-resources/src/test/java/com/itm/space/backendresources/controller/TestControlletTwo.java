package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.service.UserServiceImpl;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@WithMockUser(username = "admin", password = "admin", authorities = "ROLE_MODERATOR")
public class TestControlletTwo extends BaseIntegrationTest {
    @MockBean
    private UserServiceImpl userService;
    @MockBean
    private Keycloak keycloakClient;
    @MockBean
    private UserMapper userMapper;
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
    public void myTestCreate() throws Exception {
        UserRequest request = new UserRequest("keks", "keks@mail.ru", "keks12", "keksov", "Lokos");

        when(keycloakClient.realm(anyString())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(userResourceMock);
        when(userResourceMock.create(any(UserRepresentation.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusInfo()).thenReturn(Response.Status.CREATED);

        keycloakClient.realm("ITM");
        realmResourceMock.users();
        userResourceMock.create(mockUserRepresentation);

        verify(keycloakClient).realm(anyString());
        verify(realmResourceMock).users();
        verify(userResourceMock).create(any(UserRepresentation.class));

        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void getUserById() throws Exception {
        UUID userId = UUID.randomUUID();

        when(keycloakClient.realm(anyString())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(userResourceMock);
        when(userResourceMock.get(String.valueOf(any(UUID.class)))).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(mockUserRepresentation);

        keycloakClient.realm("ITM");
        realmResourceMock.users();
        userResourceMock.get(String.valueOf(userId));

        verify(keycloakClient).realm(anyString());
        verify(realmResourceMock).users();
        verify(userResourceMock).get(anyString());

        when(mockUserResource.roles()).thenReturn(mockRoleMapping);
        when(mockRoleMapping.getAll()).thenReturn(mockMappingRepresentation);
        when(mockMappingRepresentation.getRealmMappings()).thenReturn(mockUserRoles);

        when(mockUserResource.groups()).thenReturn(mockUserGroups);

        UserRepresentation retrievedUserRepresentation = mockUserResource.toRepresentation();
        List<RoleRepresentation> retrievedUserRoles = mockUserResource.roles().getAll().getRealmMappings();
        List<GroupRepresentation> retrievedUserGroups = mockUserResource.groups();

        verify(mockUserResource).toRepresentation();

        verify(mockUserResource).roles();
        verify(mockRoleMapping).getAll();
        verify(mockMappingRepresentation).getRealmMappings();

        verify(mockUserResource).groups();

        assertNotNull(retrievedUserRepresentation);
        assertNotNull(retrievedUserRoles);
        assertNotNull(retrievedUserGroups);


        when(userMapper.userRepresentationToUserResponse(any(UserRepresentation.class), anyList(), anyList())).thenReturn(mockUserResponse);
        UserResponse userResponse = userMapper.userRepresentationToUserResponse(mockUserRepresentation, mockUserRoles, mockUserGroups);
        verify(userMapper).userRepresentationToUserResponse(any(UserRepresentation.class), anyList(), anyList());
        assertNotNull(userResponse);

        mvc.perform(requestWithContent(get("/api/users/{id}", userId), userId))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testHello() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().string("admin"));
    }

}
