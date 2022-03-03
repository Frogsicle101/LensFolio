package nz.ac.canterbury.seng302.portfolio.controller;

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
    public String account(Model model) {

        addModelAttributes(model);
      return "account";
    }

    /**
     * Helper function to add attributes to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param model The model you're adding attributes to
     */
    private void addModelAttributes(Model model) {
        /*
        These addAttribute methods inject variables that we can use in our html file
        Their values have been hard-coded for now, but they can be the result of functions!
        ideally, these would be functions like getUsername and so forth
         */
        model.addAttribute("username", "ojo26");
        model.addAttribute("email", "ojo26@uclive.ac.nz");
        model.addAttribute("fullname", "Oliver Johnson");
        model.addAttribute("nickname", "Cromulus 'The Cube' Cronson");
        model.addAttribute("pronouns", "he/him/mr code goblin");
        model.addAttribute("userBio", "The guy whose brain is wrinkling by understanding Thymeleaf");
    }
}
