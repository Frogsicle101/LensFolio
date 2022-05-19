package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.PasswordRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;

import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {


    private final ProjectRepository projectRepository = mock(ProjectRepository.class);

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
        ModelAndView model = accountController.register();

        Assertions.assertTrue(model.hasView());
        Assertions.assertTrue(model.getModel().containsKey("alphaSpacesRegex"));
        Assertions.assertTrue(model.getModel().containsKey("alphaSpacesRegexCanBeEmpty"));
        Assertions.assertTrue(model.getModel().containsKey("userNameRegex"));
        Assertions.assertTrue(model.getModel().containsKey("emailRegex"));
        Assertions.assertTrue(model.getModel().containsKey("bioRegex"));
        Assertions.assertTrue(model.getModel().containsKey("passwordRegex"));
        Assertions.assertTrue(model.getModel().containsKey("pronounRegex"));
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







    @Test
    void testGetAccount() {
        ModelAndView modelAndView = accountController.account(principal);

        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertTrue(modelAndView.getModel().containsKey("alphaSpacesRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("alphaSpacesRegexCanBeEmpty"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("userNameRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("emailRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("bioRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("passwordRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("pronounRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("user"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("membersince"));

    }


    @Test
    void testEditAccount(){
        UserRequest userRequest = new UserRequest("testUser", "password");
        userRequest.setFirstname("Test");
        userRequest.setLastname("User");
        userRequest.setEmail("Test@Test.com");
        EditUserResponse.Builder editUserResponse = EditUserResponse.newBuilder();
        editUserResponse.setIsSuccess(true);
        editUserResponse.build();
        Mockito.when(mockClientService.editUser(Mockito.any())).thenReturn(editUserResponse.build());
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void testEditAccountBadNickname(){
        UserRequest userRequest = new UserRequest("testUser", "password");
        userRequest.setFirstname("Test");
        userRequest.setLastname("User");
        userRequest.setEmail("Test@Test.com");
        userRequest.setNickname("@");
        EditUserResponse.Builder editUserResponse = EditUserResponse.newBuilder();
        editUserResponse.setIsSuccess(true);
        editUserResponse.build();
        Mockito.when(mockClientService.editUser(Mockito.any())).thenReturn(editUserResponse.build());
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Field(s) not matching patterns", response.getBody());

    }

    @Test
    void testEditAccountBadMiddlename(){
        UserRequest userRequest = new UserRequest("testUser", "password");
        userRequest.setFirstname("Test");
        userRequest.setLastname("User");
        userRequest.setEmail("Test@Test.com");
        userRequest.setMiddlename("@");
        EditUserResponse.Builder editUserResponse = EditUserResponse.newBuilder();
        editUserResponse.setIsSuccess(true);
        editUserResponse.build();
        Mockito.when(mockClientService.editUser(Mockito.any())).thenReturn(editUserResponse.build());
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Field(s) not matching patterns", response.getBody());

    }

    @Test
    void testEditAccountBadPronouns(){
        UserRequest userRequest = new UserRequest("testUser", "password");
        userRequest.setFirstname("Test");
        userRequest.setLastname("User");
        userRequest.setEmail("Test@Test.com");
        userRequest.setPersonalPronouns("@");
        EditUserResponse.Builder editUserResponse = EditUserResponse.newBuilder();
        editUserResponse.setIsSuccess(true);
        editUserResponse.build();
        Mockito.when(mockClientService.editUser(Mockito.any())).thenReturn(editUserResponse.build());
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Field(s) not matching patterns", response.getBody());

    }

    @Test
    void testEditAccountBadBio(){
        UserRequest userRequest = new UserRequest("testUser", "password");
        userRequest.setFirstname("Test");
        userRequest.setLastname("User");
        userRequest.setEmail("Test@Test.com");
        userRequest.setBio("@");
        EditUserResponse.Builder editUserResponse = EditUserResponse.newBuilder();
        editUserResponse.setIsSuccess(true);
        editUserResponse.build();
        Mockito.when(mockClientService.editUser(Mockito.any())).thenReturn(editUserResponse.build());
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Field(s) not matching patterns", response.getBody());

    }

    @Test
    void testEditAccountBadRequest(){
        UserRequest userRequest = new UserRequest("testUser", "password");
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void testEditAccountFailToChange(){
        UserRequest userRequest = new UserRequest("testUser", "password");
        userRequest.setFirstname("Test");
        userRequest.setLastname("User");
        userRequest.setEmail("Test@Test.com");
        EditUserResponse.Builder editUserResponse = EditUserResponse.newBuilder();
        editUserResponse.setIsSuccess(false);
        editUserResponse.build();
        Mockito.when(mockClientService.editUser(Mockito.any())).thenReturn(editUserResponse.build());
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }


    @Test
    void testEditPassword(){
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setNewPassword("password");
        passwordRequest.setConfirmPassword("password");
        passwordRequest.setOldPassword("password");

        ChangePasswordResponse.Builder changePasswordresponse = ChangePasswordResponse.newBuilder();
        changePasswordresponse.setIsSuccess(true);
        Mockito.when(mockClientService.changeUserPassword(Mockito.any())).thenReturn(changePasswordresponse.build());
        ResponseEntity<Object> response = accountController.editPassword(principal, passwordRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testEditPasswordFailToChange(){
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setNewPassword("password");
        passwordRequest.setConfirmPassword("password");
        passwordRequest.setOldPassword("password");

        ChangePasswordResponse.Builder changePasswordresponse = ChangePasswordResponse.newBuilder();
        changePasswordresponse.setIsSuccess(false);
        Mockito.when(mockClientService.changeUserPassword(Mockito.any())).thenReturn(changePasswordresponse.build());
        ResponseEntity<Object> response = accountController.editPassword(principal, passwordRequest);
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    void testEditPasswordPasswordsDontMatch(){
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setNewPassword("password");
        passwordRequest.setConfirmPassword("password2");
        passwordRequest.setOldPassword("password");

        ChangePasswordResponse.Builder changePasswordresponse = ChangePasswordResponse.newBuilder();
        changePasswordresponse.setIsSuccess(false);
        Mockito.when(mockClientService.changeUserPassword(Mockito.any())).thenReturn(changePasswordresponse.build());
        ResponseEntity<Object> response = accountController.editPassword(principal, passwordRequest);
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        Assertions.assertEquals("Confirm password does not match new password.", response.getBody());
    }


    @Test
    void testDeleteProfileImg(){

        DeleteUserProfilePhotoResponse.Builder delete = DeleteUserProfilePhotoResponse.newBuilder();
        delete.setIsSuccess(true);
        Mockito.when(mockClientService.deleteUserProfilePhoto(Mockito.any())).thenReturn(delete.build());
        ResponseEntity<String> response = accountController.deleteProfilePhoto(principal);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }




}
