package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.ReadableTimeService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
            Model model) {
        /*
        These addAttribute methods inject variables that we can use in our html file
        Their values have been hard-coded for now, but they can be the result of functions!
        ideally, these would be functions like getUsername and so forth
         */
        int id = Integer.parseInt(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND"));
        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(id);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(request.build());
        //todo add middle name here (don't forget that users might not have a middle name, see Aidan if confused
        model.addAttribute("username", "Username: " + userResponse.getUsername());
        model.addAttribute("email", "Email: " + userResponse.getEmail());
        String fullname = userResponse.getFirstName() + " " + userResponse.getMiddleName() + " " + userResponse.getLastName();
        model.addAttribute("fullname", "Name: " + fullname.replaceAll(" +", " "));
        model.addAttribute("nickname", "Nickname: " + userResponse.getNickname());
        model.addAttribute("pronouns", "Pronouns: " + userResponse.getPersonalPronouns());
        model.addAttribute("userBio", "Bio: " + userResponse.getBio());

        String memberSince = "Member Since: "
                + ReadableTimeService.getReadableDate(userResponse.getCreated())
                + " (" + ReadableTimeService.getReadableTimeSince(userResponse.getCreated()) + ")";
        model.addAttribute("membersince", memberSince);

        String rolesList = "";
        for (int i = 0; i < userResponse.getRolesCount(); i++) {
            rolesList += userResponse.getRoles(i) + "  ";
        }
        model.addAttribute("roles", "Roles: " + rolesList);
    }
}
