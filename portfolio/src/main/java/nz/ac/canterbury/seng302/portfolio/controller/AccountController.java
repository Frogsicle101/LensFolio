package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.PasswordRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.service.ReadableTimeService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;


/**
 * Controller class for the account page
 *
 * This page is responsible for displaying user information
 */
@Controller
public class AccountController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String alphaSpacesRegex = "([a-zA-Z]+\s?)+";
    private static final String alphaSpacesRegexCanBeEmpty = "([a-zA-Z]*\s?)+";
    private static final String userNameRegex = "([a-zA-Z0-9!#$%&'*+/=?^_`{|}~]+)";
    private static final String emailRegex = "^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
    private static final String bioRegex = "([a-zA-Z0-9.,'\"]*\\s?)+";
    private static final String passwordRegex = "([a-zA-Z0-9!#$%&'*+/=?^_`{|}~]+)";
    private static final String pronounRegex = "([a-zA-Z/]*\\s?)+";

    /**
     * This method is responsible for populating the account page template
     * It adds in variables to the html template, as well as the values of those variables
     * It then returns the 'filled in' html template, to be displayed in a web browser
     * <br>
     * Once a user class is created, we will want to supply this page with the specific user that is viewing it
     * <br>
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return the Thymeleaf template
     */
    @RequestMapping("/account")
    public String account(
            @AuthenticationPrincipal AuthState principal,
            ModelMap model,
            HttpServletRequest request,
            @ModelAttribute(name = "editDetailsForm") UserRequest editInfo,
            @ModelAttribute(name = "editPasswordForm") PasswordRequest passInfo,
            @ModelAttribute(name = "detailChangeMessage") String detailChangeMessage,
            @ModelAttribute(name = "passwordChangeMessage") String passwordChangeMessage
    ) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        logger.info("REQUEST /account - retrieving account details for user {}",userId);
        addModelAttributes(userId, request, model);
        logger.info("Account details populated for {}", userId);
        return "account";
    }

    /**
     * Returns the template for the register page
     * @return Thymeleaf template for the register screen
     */
    @GetMapping("/register")
    public String register() {
        logger.info("GET REQUEST /register - get register page");
        return "accountRegister";
    }

    /**
     * Called when a user attempts to register a new account, if the registration is successful forwards a user to
     * their account page, otherwise informs the user why their attempt was unsuccessful.
     *
     * @param userRequest - A UserRequest object used to retrieve user input from the html.
     * @return view - the html page redirected to, either account details on successful registration or register on failure.
     */
    @PostMapping("/register")
    public ResponseEntity<Object> attemptRegistration(
            @ModelAttribute(name="registerForm") UserRequest userRequest
    ) {
        logger.info("POST REQUEST /register - attempt to register new user");
        try{


            ResponseEntity<Object> checkUserRequest = checkUserRequest(userRequest); // Checks that the userRequest object passes all checks
            if (checkUserRequest.getStatusCode() == HttpStatus.BAD_REQUEST) {
                logger.warn("Registration Failed: {}", checkUserRequest.getBody());
                return checkUserRequest;
            }

            // Make UserRegisterRequest and send to Server
            UserRegisterResponse registerReply = userAccountsClientService.register(createUserRegisterRequest(userRequest));
            // Attempt to login new user
            if (registerReply.getIsSuccess()) {
                logger.info("Registration Success: {}", registerReply.getMessage());
                logger.info("Log in new user");
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                logger.info("Registration Failed: {}", registerReply.getMessage());
                return new ResponseEntity<>(registerReply.getMessage(), HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (Exception err) {
            logger.error("Registration Failed: {}",err.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    /**
     * Checks that the UserRequest follows the required patterns and contains everything needed
     * @param userRequest the UserRequest
     * @return ResponseEntity, either an accept, or a not accept with message as to what went wrong
     */
    private ResponseEntity<Object> checkUserRequest(UserRequest userRequest) {
        String firstname = userRequest.getFirstname();
        String middlename = userRequest.getMiddlename();
        String lastname = userRequest.getLastname();
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();
        String email = userRequest.getEmail();
        String nickname = userRequest.getNickname();
        String pronouns = userRequest.getPersonalPronouns();
        String bio = userRequest.getBio();


        if (firstname == null // Checks that all necessary information is there.
                || lastname == null
                || username == null
                || password == null
                || email == null) {
            return new ResponseEntity<>("Missing fields", HttpStatus.BAD_REQUEST);

        }

        if(middlename == null){
            userRequest.setMiddlename("");
        }
        if (nickname == null) {
            userRequest.setNickname("");
        }
        if (bio == null) {
            userRequest.setBio("");
        }
        if (pronouns == null){
            userRequest.setPersonalPronouns("");
        }


        // Checks that the strings passed through from the front-end are in formats that are acceptable with regex checks.
        if (!firstname.matches(alphaSpacesRegex)
                || !lastname.matches(alphaSpacesRegex)
                || !username.matches(userNameRegex)
                || !email.matches(emailRegex)
                || !password.matches(passwordRegex)
                // Checks if the non-necessary fields have strings in them, if they do then they need to match the pattern that is acceptable.
                || nickname != null && !nickname.matches(alphaSpacesRegexCanBeEmpty)
                || middlename != null && !middlename.matches(alphaSpacesRegexCanBeEmpty)
                || pronouns != null && !pronouns.matches(pronounRegex)
                || bio != null && !bio.matches(bioRegex)) {

            return new ResponseEntity<>("Field(s) not matching patterns",HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * Checks that the UserRequest follows the required patterns and contains everything needed
     * @param userRequest the UserRequest without password
     * @return ResponseEntity, either an accept, or a not accept with message as to what went wrong
     */
    private ResponseEntity<Object> checkUserRequestNoPasswordOrUser(UserRequest userRequest) {
        String firstname = userRequest.getFirstname();
        String middlename = userRequest.getMiddlename();
        String lastname = userRequest.getLastname();
        String email = userRequest.getEmail();
        String nickname = userRequest.getNickname();
        String pronouns = userRequest.getPersonalPronouns();
        String bio = userRequest.getBio();


        if (firstname == null // Checks that all necessary information is there.
                || lastname == null
                || email == null) {
            return new ResponseEntity<>("Missing fields", HttpStatus.BAD_REQUEST);

        }

        if(middlename == null){
            userRequest.setMiddlename("");
        }
        if (nickname == null) {
            userRequest.setNickname("");
        }
        if (bio == null) {
            userRequest.setBio("");
        }
        if (pronouns == null){
            userRequest.setPersonalPronouns("");
        }


        // Checks that the strings passed through from the front-end are in formats that are acceptable with regex checks.
        if (!firstname.matches(alphaSpacesRegex)
                || !lastname.matches(alphaSpacesRegex)
                || !email.matches(emailRegex)
                // Checks if the non-necessary fields have strings in them, if they do then they need to match the pattern that is acceptable.
                || nickname != null && !nickname.matches(alphaSpacesRegexCanBeEmpty)
                || middlename != null && !middlename.matches(alphaSpacesRegexCanBeEmpty)
                || pronouns != null && !pronouns.matches(pronounRegex)
                || bio != null && !bio.matches(bioRegex)) {

            return new ResponseEntity<>("Field(s) not matching patterns",HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }



    /**
     * Entry point for editing account details
     * This also handle the logic for changing the account details\
     * @param principal The authentication state
     * @param editInfo The thymeleaf-created form object
     * @return a redirect to the main /edit endpoint
     */
    @PostMapping("/edit/details")
    public ResponseEntity<Object> editDetails(
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editDetailsForm") UserRequest editInfo
    ) {
        try{
            ResponseEntity<Object> checkUserRequest = checkUserRequestNoPasswordOrUser(editInfo); // Checks that the userRequest object passes all checks
            if (checkUserRequest.getStatusCode() == HttpStatus.NOT_ACCEPTABLE) {
                logger.warn("Editing Failed: {}",checkUserRequest.getBody());
                return checkUserRequest;
            }

            EditUserRequest.Builder editRequest = EditUserRequest.newBuilder();
            int userId = PrincipalAttributes.getIdFromPrincipal(principal);
            logger.info(" POST REQUEST /edit/details - update account details for user {}",userId);

            editRequest.setUserId(userId)
                    .setFirstName(editInfo.getFirstname())
                    .setMiddleName(editInfo.getMiddlename())
                    .setLastName(editInfo.getLastname())
                    .setNickname(editInfo.getNickname())
                    .setBio(editInfo.getBio())
                    .setPersonalPronouns(editInfo.getPersonalPronouns())
                    .setEmail(editInfo.getEmail());
            EditUserResponse reply = userAccountsClientService.editUser(editRequest.build());
            if (reply.getIsSuccess()) {
                logger.info("Successfully updated details for user {}", userId);
            } else {
                logger.error("Failed to update details for user {}", userId);
                return new ResponseEntity<>(reply.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return new ResponseEntity<>(reply.getMessage() ,HttpStatus.OK);
        } catch (Exception err){
            logger.error("/edit/details ERROR: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    /**
     * Entry point for editing the password
     * This also handle the logic for changing the password
     * Note: this injects an attribute called "passwordchangemessage" into the template it redirects to
     * @param attributes extra attributes that are being given along with the redirect
     * @param principal The authentication state
     * @param editInfo the thymeleaf-created form object
     * @return a redirect to the main /edit endpoint
     */
    @PostMapping("/edit/password")
    public ResponseEntity<Object> editPassword(
            RedirectAttributes attributes,
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editPasswordForm") PasswordRequest editInfo
    ){
        try {
            int userId = PrincipalAttributes.getIdFromPrincipal(principal);
            logger.info("POST REQUEST /edit/password - update password for user {}", userId);
            ChangePasswordRequest.Builder changePasswordRequest = ChangePasswordRequest.newBuilder();

            ChangePasswordResponse changePasswordResponse;
            if (editInfo.getNewPassword().equals(editInfo.getConfirmPassword())) {
                logger.info("New password and confirm password match, requesting change password service ({})",userId);
                //Create request
                changePasswordRequest.setUserId(userId)
                        .setCurrentPassword(editInfo.getOldPassword())
                        .setNewPassword(editInfo.getNewPassword());
                changePasswordResponse = userAccountsClientService.changeUserPassword(changePasswordRequest.build());
                if (changePasswordResponse.getIsSuccess()) {
                    logger.info("Password change success: {}",changePasswordResponse.getMessage());
                } else {
                    logger.warn("Password change failed: {}",changePasswordResponse.getMessage());
                    return new ResponseEntity<>(changePasswordResponse.getMessage(), HttpStatus.NOT_ACCEPTABLE);
                }


            } else {
                logger.info("Confirm password does not match new password. Cancelling password change for {}",userId);
                // Tell the user to confirm their passwords match
                return new ResponseEntity<>("Confirm password does not match new password.", HttpStatus.NOT_ACCEPTABLE);
            }
            //Give the user the response from the IDP
            return new ResponseEntity<>(changePasswordResponse.getMessage(), HttpStatus.OK);

        } catch (Exception err) {
            logger.error("/edit/password Error {}", err.getMessage());
            return new ResponseEntity<>(err.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }






    @DeleteMapping("/deleteProfileImg")
    public ResponseEntity<String> deleteProfilePhoto(
            @AuthenticationPrincipal AuthState principal
    ) {
        logger.info("Endpoint reached: DELETE /deleteProfileImg");
        int id = PrincipalAttributes.getIdFromPrincipal(principal);

        DeleteUserProfilePhotoRequest deleteRequest = DeleteUserProfilePhotoRequest.newBuilder().setUserId(id).build();

        DeleteUserProfilePhotoResponse response = userAccountsClientService.deleteUserProfilePhoto(deleteRequest);
        if (response.getIsSuccess()) {
            logger.info("Profile photo deleted - {}", response.getMessage());
        } else {
            logger.warn("Didn't delete profile photo - {}", response.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Takes a UserRequest object populated from a registration form and returns a UserRegisterRequest to send to the server
     *
     * @param userRequest - A UserRequest object populated from a accountRegister.html form
     * @return userRegisterRequest - a populated userRegisterRequest from the user_accounts.proto format
     */
    private UserRegisterRequest createUserRegisterRequest(UserRequest userRequest) {
        logger.info("Creating user register request from UserRequest");
        UserRegisterRequest.Builder userRegisterRequest = UserRegisterRequest.newBuilder();
        userRegisterRequest.setUsername(userRequest.getUsername())
                .setPassword(userRequest.getPassword())
                .setFirstName(userRequest.getFirstname())
                .setMiddleName(userRequest.getMiddlename())
                .setLastName(userRequest.getLastname())
                .setEmail(userRequest.getEmail())
                .setBio(userRequest.getBio())
                .setPersonalPronouns(userRequest.getPersonalPronouns())
                .setNickname(userRequest.getNickname());
        return userRegisterRequest.build();
    }

    public void setUserAccountsClientService(UserAccountsClientService service) {
        this.userAccountsClientService = service;
    }


    /**
     * Helper function to add attributes to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     * <p>
     * This is really just to make the code a bit nicer to look at
     * <br>
     * @param userId - the userId of the account whose details are being retrieved
     * @param model - The model you're adding attributes to
     */
    private void addModelAttributes(
            int userId,
            HttpServletRequest request,
            ModelMap model) {
        // NOTE: no logger as this is a helper function and calling function logs the input
        /*
        These addAttribute methods inject variables that we can use in our html file
         */
        UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                .setId(userId)
                .build());

        // For setting the profile image
        String ip = request.getLocalAddr();
        String url = "http://" + ip + ":9001/" + userResponse.getProfileImagePath();
        model.addAttribute("profileImageUrl", url);

        model.addAttribute("username", userResponse.getUsername());
        model.addAttribute("email", userResponse.getEmail());
        model.addAttribute("firstName", userResponse.getFirstName());
        model.addAttribute("middleName", userResponse.getMiddleName());
        model.addAttribute("lastName", userResponse.getLastName());
        model.addAttribute("nickname", userResponse.getNickname());
        model.addAttribute("pronouns", userResponse.getPersonalPronouns());
        model.addAttribute("userBio", userResponse.getBio());
        String rolesList = "";
        for (int i = 0; i < userResponse.getRolesCount(); i++) {
            rolesList += userResponse.getRoles(i) + "  ";
        }
        model.addAttribute("roles", rolesList);

        String memberSince =
                ReadableTimeService.getReadableDate(userResponse.getCreated())
                        + " (" + ReadableTimeService.getReadableTimeSince(userResponse.getCreated()) + ")";
        model.addAttribute("membersince", memberSince);
    }
}




