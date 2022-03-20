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
     * <p>
     * Once a user class is created, we will want to supply this page with the specific user that is viewing it
     *
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return the Thymeleaf template
     */
    @RequestMapping("/account")
    public String account(
            @AuthenticationPrincipal AuthState principal,
            ModelMap model,
            @ModelAttribute(name = "editDetailsForm") UserRequest editInfo,
            @ModelAttribute(name = "editPasswordForm") PasswordRequest passInfo,
            @ModelAttribute(name = "detailChangeMessage") String detailChangeMessage,
            @ModelAttribute(name = "passwordChangeMessage") String passwordChangeMessage
    ) {

        addModelAttributes(principal, model);
        return "account";
    }

    /**
     * Helper function to add attributes to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     * <p>
     * This is really just to make the code a bit nicer to look at
     *
     * @param model The model you're adding attributes to
     */
    private void addModelAttributes(
            AuthState principal,
            ModelMap model) {
        /*
        These addAttribute methods inject variables that we can use in our html file
         */
        int id = Integer.parseInt(PrincipalAttributes.getClaim(principal, "nameid"));
        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(id);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(request.build());
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




