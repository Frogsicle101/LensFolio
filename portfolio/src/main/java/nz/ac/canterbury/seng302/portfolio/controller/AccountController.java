package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.PasswordRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.service.ReadableTimeService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Controller class for the account page
 *
 * This page is responsible for displaying user information
 */
@Controller
public class AccountController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    /**
     * This method is responsible for populating the account page template
     * It adds in variables to the html template, as well as the values of those variables
     * It then returns the 'filled in' html template, to be displayed in a web browser
     *
     * Once a user class is created, we will want to supply this page with the specific user that is viewing it
     *
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return the Thymeleaf template
     */
    @RequestMapping("/account")
    public String account(
            @AuthenticationPrincipal AuthState principal,
            ModelMap model
            @ModelAttribute(name="editDetailsForm") UserRequest editInfo,
            @ModelAttribute(name="editPasswordForm") PasswordRequest passInfo,
            Model model
    ) {

        addModelAttributes(principal, model);
      return "account";
    }

    /**
     * Helper function to add attributes to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param model The model you're adding attributes to
     */
    private void addModelAttributes(
            AuthState principal,
            ModelMap model) {
        /*
        These addAttribute methods inject variables that we can use in our html file
         */
        int id = Integer.parseInt(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND"));
        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(id);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(request.build());
        model.addAttribute("username", "Username: " + userResponse.getUsername());
        model.addAttribute("email", "Email: " + userResponse.getEmail());
        String fullname = userResponse.getFirstName() + " " + userResponse.getMiddleName() + " " + userResponse.getLastName();
        model.addAttribute("fullname", "Name: " + fullname.replaceAll(" +", " "));
        model.addAttribute("nickname", "Nickname: " + userResponse.getNickname());
        model.addAttribute("pronouns", "Pronouns: " + userResponse.getPersonalPronouns());
        model.addAttribute("userBio", "Bio: " + userResponse.getBio());
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

    /**
     * Entry point for editing account details
     * @param request the HTTP request
     * @param response the HTTP response
     * @param editInfo The thymeleaf-created form object
     * @param model The thymeleaf model
     * @return a redirect to the main /edit endpoint
     */
    @PostMapping("/edit/details")
    public ModelAndView editDetails(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editDetailsForm") UserRequest editInfo,
            Model model
    ) {
        EditUserRequest.Builder editRequest = EditUserRequest.newBuilder();

        Integer id = Integer.valueOf(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("-100"));

        editRequest.setUserId(id)
                .setFirstName(editInfo.getFirstname())
                .setMiddleName(editInfo.getMiddlename())
                .setLastName(editInfo.getLastname())
                .setNickname(editInfo.getNickname())
                .setBio(editInfo.getBio())
                .setPersonalPronouns(editInfo.getPersonalPronouns())
                .setEmail(editInfo.getEmail());
        EditUserResponse reply = userAccountsClientService.editUser(editRequest.build());
        System.out.println(reply);
        //Since they're at a different endpoint, redirect back to the main edit endpoint
        return new ModelAndView("redirect:/account");
    }



    /**
     * Entry point for editing the password
     * @param request the HTTP request
     * @param response the HTTP response
     * @param editInfo the thymeleaf-created form object
     * @param model the thymeleaf model
     * @return a redirect to the main /edit endpoint
     */
    @PostMapping("/edit/password")
    public ModelAndView editPassword(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editPasswordForm") PasswordRequest editInfo,
            Model model
    ){
        ChangePasswordRequest.Builder changePasswordRequest = ChangePasswordRequest.newBuilder();
        // Get user ID, this really needs to be a method
        Integer id = Integer.valueOf(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("-100"));

        ChangePasswordResponse changePasswordResponse;
        if (editInfo.getNewPassword().equals(editInfo.getConfirmPassword())) {
            //Create request
            changePasswordRequest.setUserId(id)
                    .setCurrentPassword(editInfo.getOldPassword())
                    .setNewPassword(editInfo.getNewPassword());
            changePasswordResponse = userAccountsClientService.changeUserPassword(changePasswordRequest.build());
            System.out.println(changePasswordResponse.getMessage());
        } else {
            // Do something with this (user message)
            System.out.println("Confirm password does not match new password.");
        }
        //Since they're at a different endpoint, redirect back to the main edit endpoint
        return new ModelAndView("redirect:/account");
    }
}
