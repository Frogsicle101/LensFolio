package nz.ac.canterbury.seng302.portfolio.controller;

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
                .filter(claim -> claim.getType().equals("id"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND"));
        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(id);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(request.build());
        model.addAttribute("username", userResponse.getUsername());
        model.addAttribute("email", userResponse.getEmail());
        model.addAttribute("fullname", userResponse.getFirstName() + " " + userResponse.getLastName());
        model.addAttribute("nickname", userResponse.getNickname());
        model.addAttribute("pronouns", userResponse.getNickname());
        model.addAttribute("userBio", userResponse.getBio());

    }
}
