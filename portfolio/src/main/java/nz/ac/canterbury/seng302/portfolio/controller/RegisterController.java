package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


/**
 * Controller class for the register page
 *
 * This page is responsible for allowing users to register a new account
 */
@Controller
public class RegisterController {

    /**
     *
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Thymeleaf template for the register screen
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("test", "test"); //to remove just for testing
        return "register";
    }

    //todo method to receive the inputs after registration and to perform checks on them, then redirect to account page
}