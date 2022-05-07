package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;

import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);

    @Autowired
    private SprintRepository sprintRepository;


    private final AccountController accountController = new AccountController();
    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);
    private final AuthState principal = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();

    @BeforeEach
    public void beforeAll() {
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
        UserResponse user = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal, mockClientService)).thenReturn(user);
        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        when(mockClientService.getUserAccountById(userByIdRequest)).thenReturn(user);

        UserRegisterResponse userRegisterResponse = UserRegisterResponse.newBuilder().setIsSuccess(true).build();
        when(mockClientService.register(any(UserRegisterRequest.class))).thenReturn(userRegisterResponse);

        accountController.setUserAccountsClientService(mockClientService);
        Project project = new Project("test");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    }

    @Test
    void testGetRegister() {
        String model = accountController.register();
        Assertions.assertEquals("accountRegister", model);
    }

    @Test
    void testAttemptRegistrationNotAcceptable() {
        UserRequest userRequest = new UserRequest("TestCase", "Password");
        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternEmail() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternFirstname() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test!");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void testAttemptRegistrationIncorrectPatternMiddlename() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename("Mcgregor gregorich!");


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternLastname() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing@");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternUsername() {

        UserRequest userRequest = new UserRequest("TestCase CaseTest", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternPassword() {

        UserRequest userRequest = new UserRequest("TestCase", "Password Not Correct");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternBio() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio("!!!@#! @!@# ASD");
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternNickname() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname("Mr Jeeves!");
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternPronouns() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns("He/Him Them!");
        userRequest.setMiddlename(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationMissingFields() {

        UserRequest userRequest = new UserRequest("", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void testAttemptRegistrationRequiredNullFields() {

        UserRequest userRequest = new UserRequest(null, "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationNullFieldsOtherThanRequired() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setMiddlename(null);
        userRequest.setNickname(null);
        userRequest.setBio(null);
        userRequest.setPersonalPronouns(null);


        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }



}
