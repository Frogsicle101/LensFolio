package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.PasswordRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for the edit page
 */
@Controller
public class EditController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The main/entry endpoint into the edit page
     * I'm not entirely sure why this method in particular needs to have the form-objects created
     * But the page won't run without it
     * My best guess is it's something to do with the endpoints being different
     *
     * @param principal The authentication state
     * @param editInfo A thymeleaf-created object
     * @param passInfo A thymeleaf-created object
     * @param detailChangeMessage This is actually an injected thymeleaf attribute from editDetails. It is a message
     *                            detailing the success/errors of the detail change attempt
     * @param passwordChangeMessage This is an injected thymeleaf attribute from editPassword. It is a message
     *                              detailing the success/errors of the password change attempt.
     * @param model The thymeleaf model
     * @return The thymeleaf template
     */
    @RequestMapping("/edit")
    public String edit(
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editDetailsForm") UserRequest editInfo,
            @ModelAttribute(name="editPasswordForm") PasswordRequest passInfo,
            @ModelAttribute(name="detailChangeMessage") String detailChangeMessage,
            @ModelAttribute(name="passwordChangeMessage") String passwordChangeMessage,
            ModelMap model
    ) {
        /*
        We want to fill in the form details with what the user already has
        so let's grab all those details and put them in the model
         */
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        logger.info("REQUEST /edit - retrieving account details for user " + userId);
        addModelAttributes(userId, model);
        logger.info("Edit account details populated for " + userId);
        return "accountEdit";
    }

    /**
     * Helper function to add attributes to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param userId The user id of the account being edited
     * @param model The model you're adding attributes to
     */
    private void addModelAttributes(
            int userId,
            ModelMap model) {
        /*
        These addAttribute methods inject variables that we can use in our html file
        Their values have been hard-coded for now, but they can be the result of functions!
        ideally, these would be functions like getUsername and so forth
         */
        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(userId);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(request.build());
        model.addAttribute("username", "Username:" + userResponse.getUsername());
        model.addAttribute("email", userResponse.getEmail());
        model.addAttribute("firstname", userResponse.getFirstName());
        model.addAttribute("middlename", userResponse.getMiddleName());
        model.addAttribute("lastname", userResponse.getLastName());
        model.addAttribute("nickname", userResponse.getNickname());
        model.addAttribute("pronouns", userResponse.getPersonalPronouns());
        model.addAttribute("userBio", userResponse.getBio());

    }

    /**
     * Entry point for editing account details
     * This also handle the logic for changing the account details\
     * <br>
     * Note: this injects an attribute called "detailchangemessage" into the template it redirects to
     * <br>
     * @param attributes extra attributes that are being given along with the redirect
     * @param request the HTTP request
     * @param response the HTTP response
     * @param principal The authentication state
     * @param editInfo The thymeleaf-created form object
     * @param model The thymeleaf model
     * @return a redirect to the main /edit endpoint
     */
    @PostMapping("/edit/details")
    public ModelAndView editDetails(
            RedirectAttributes attributes,
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editDetailsForm") UserRequest editInfo,
            ModelMap model
    ) {
        EditUserRequest.Builder editRequest = EditUserRequest.newBuilder();
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        logger.info(" POST REQUEST /edit/details - update account details for user " + userId);

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
            logger.info("Successfully updated details for user " + userId);
        } else {
            logger.error("Failed to update details for user " + userId);
        }
        //Add in the message for changing details
        attributes.addFlashAttribute("detailChangeMessage", reply.getMessage());
        //Since they're at a different endpoint, redirect back to the main edit endpoint
        return new ModelAndView("redirect:/account");
    }

    /**
     * Entry point for editing the password
     * This also handle the logic for changing the password
     * Note: this injects an attribute called "passwordchangemessage" into the template it redirects to
     * @param attributes extra attributes that are being given along with the redirect
     * @param request the HTTP request
     * @param response the HTTP response
     * @param principal The authentication state
     * @param editInfo the thymeleaf-created form object
     * @param model the thymeleaf model
     * @return a redirect to the main /edit endpoint
     */
    @PostMapping("/edit/password")
    public ModelAndView editPassword(
            RedirectAttributes attributes,
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editPasswordForm") PasswordRequest editInfo,
            ModelMap model
    ){
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        logger.info("POST REQUEST /edit/password - update password for user " + userId);
        ChangePasswordRequest.Builder changePasswordRequest = ChangePasswordRequest.newBuilder();

        ChangePasswordResponse changePasswordResponse;
        if (editInfo.getNewPassword().equals(editInfo.getConfirmPassword())) {
            logger.info("New password and confirm password match, requesting change password service (" + userId + ")");
            //Create request
            changePasswordRequest.setUserId(userId)
                    .setCurrentPassword(editInfo.getOldPassword())
                    .setNewPassword(editInfo.getNewPassword());
            changePasswordResponse = userAccountsClientService.changeUserPassword(changePasswordRequest.build());
            if (changePasswordResponse.getIsSuccess()) {
                logger.info("Password change success: " + changePasswordResponse.getMessage());
            } else {
                logger.info("Password change failed: " + changePasswordResponse.getMessage());
            }
            //Give the user the response from the IDP
            //Flash attributes aren't visible in the URL, which is why this is a flash attribute
            attributes.addFlashAttribute("successMessage",
                    changePasswordResponse.getMessage());

        } else {
            logger.info("Confirm password does not match new password. Cancelling password change for " + userId);
            // Tell the user to confirm their passwords match
            attributes.addFlashAttribute("errorMessage",
                    "Confirm password does not match new password.");
        }
        //Since they're at a different endpoint, redirect back to the main edit endpoint
        return new ModelAndView("redirect:/account");
    }
}
