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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

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
        logger.info("REQUEST /account - retrieving account details for user " + userId);
        addModelAttributes(userId, request, model);
        logger.info("Account details populated for " + userId);
        return "account";
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
        model.addAttribute("profileImageUrl", userResponse.getProfileImagePath());

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


    @DeleteMapping("/deleteProfileImg")
    public ResponseEntity<String> deleteProfilePhoto(
            @AuthenticationPrincipal AuthState principal
    ) {
        logger.info("Endpoint reached: DELETE /deleteProfileImg");
        int id = PrincipalAttributes.getIdFromPrincipal(principal);

        DeleteUserProfilePhotoRequest deleteRequest = DeleteUserProfilePhotoRequest.newBuilder().setUserId(id).build();

        DeleteUserProfilePhotoResponse response = userAccountsClientService.deleteUserProfilePhoto(deleteRequest);
        if (response.getIsSuccess()) {
            logger.info("Profile photo deleted - " + response.getMessage());
        } else {
            logger.info("Didn't delete profile photo - " + response.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}




