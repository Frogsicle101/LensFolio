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
import java.io.File;
import java.io.IOException;


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
            HttpServletRequest request,
            @ModelAttribute(name = "editDetailsForm") UserRequest editInfo,
            @ModelAttribute(name = "editPasswordForm") PasswordRequest passInfo,
            @ModelAttribute(name = "detailChangeMessage") String detailChangeMessage,
            @ModelAttribute(name = "passwordChangeMessage") String passwordChangeMessage
    ) throws IOException {
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        int id = Integer.parseInt(PrincipalAttributes.getClaim(principal,"nameid"));

        userAccountsClientService.uploadProfilePhoto(new File("src/main/resources/frog.jpg"), id, "jpg");

        model.addAttribute("user", user);

        String ip = request.getLocalAddr();
        String url = "http://" + ip + ":9001/" + user.getProfileImagePath();


        model.addAttribute("profileImageUrl", url);

        String rolesList = "";
        for (int i = 0; i < user.getRolesCount(); i++) {
            rolesList += user.getRoles(i) + "  ";
        }
        model.addAttribute("roles", rolesList);

        String memberSince =
                ReadableTimeService.getReadableDate(user.getCreated())
                        + " (" + ReadableTimeService.getReadableTimeSince(user.getCreated()) + ")";
        model.addAttribute("membersince", memberSince);

        return "account";
    }
}



