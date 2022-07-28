package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.projects.repositories.GitRepoRepository;
import nz.ac.canterbury.seng302.portfolio.projects.repositories.GitRepository;
import nz.ac.canterbury.seng302.portfolio.controller.GitRepoController;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.naming.InvalidNameException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = GitRepoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GitRepoControllerTest {

    private Authentication principal;
    private GroupDetailsResponse response;
    private UserResponse userResponse;

    @MockBean
    AuthenticateClientService authenticateClientService;

    @MockBean
    UserAccountsClientService userAccountsClientService;

    @MockBean
    GroupsClientService groupsClientService;

    @MockBean
    GitRepoRepository gitRepoRepository;

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    public void beforeAll() {

    }


    @Test
    void testAddGitRepoValid() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "abcdef0123456789abcdef0123456789abcdef01"))
                .andExpect(status().isOk());
    }

    @Test
    void testAddGitRepoValidTeacher() throws Exception {
        setUserRoleToTeacher();
        setUserToNotGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "abcdef0123456789abcdef0123456789abcdef01"))
                .andExpect(status().isOk());
    }


    @Test
    void testAddGitRepoValidAdmin() throws Exception {
        setUserRoleToAdmin();
        setUserToNotGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "abcdef0123456789abcdef0123456789abcdef01"))
                .andExpect(status().isOk());
    }


    @Test
    void testAddGitRepoInvalidUser() throws Exception {
        setUserRoleToStudent();
        setUserToNotGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "abcdef0123456789abcdef0123456789abcdef01"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testAddGitRepoInvalidGroupId() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "invalid")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "access token"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidProjectId() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "invalid")
                        .param("alias", "repo alias")
                        .param("accessToken", "access token"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidAlias() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "")
                        .param("accessToken", "access token"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoNoAccessToken() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", ""))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidAccessTokenCharacters() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "usingIllegalChactersInTheAccesTokenHere!"))
                .andExpect(status().isBadRequest());
    }


    private void setUserToGroupMember() {
        response = GroupDetailsResponse.newBuilder()
                .addMembers(userResponse).build();

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);
    }


    private void setUserToNotGroupMember() {
        UserResponse emptyUserResponse = UserResponse.newBuilder().build();

        response = GroupDetailsResponse.newBuilder()
                .addMembers(emptyUserResponse).build();

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);
    }


    private void setUserRoleToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.STUDENT);
        userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
    }


    private void setUserRoleToTeacher() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("teacher").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.TEACHER);
        userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
    }


    private void setUserRoleToAdmin() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("course_administrator").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.COURSE_ADMINISTRATOR);
        userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
    }


    private void setupContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));

        SecurityContextHolder.setContext(mockedSecurityContext);
    }

    @Test
    void testRetrieveGitRepoInvalidAccessToken() throws InvalidNameException {
        ResponseEntity<Object> response = GitRepoController.retrieveGitRepo("1", "1", "this is a incorrect accessToken for test");
        List<GitRepository> gitRepos = (List<GitRepository>) response.getBody();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, gitRepos.size());
    }

    @Test
    void testRetrieveGitRepoInvalidGroupId() throws InvalidNameException {
        ResponseEntity<Object> response = GitRepoController.retrieveGitRepo("100000", "1", "this is a incorrect accessToken for test");
        List<GitRepository> gitRepos = (List<GitRepository>) response.getBody();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, gitRepos.size());
    }

    @Test
    void testRetrieveGitRepoInvalidProjectId() throws InvalidNameException {
        ResponseEntity<Object> response = GitRepoController.retrieveGitRepo("1", "100000", "this is a incorrect accessToken for test");
        List<GitRepository> gitRepos = (List<GitRepository>) response.getBody();
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, gitRepos.size());
    }

    @Test
    void testRetrieveGitRepo() throws InvalidNameException {
        ResponseEntity<Object> response = GitRepoController.retrieveGitRepo("1", "1", "abcdef0123456789abcdef0123456789abcdef01");
        List<GitRepository> gitRepos = (List<GitRepository>) response.getBody();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(1, gitRepos.size());
    }

}
