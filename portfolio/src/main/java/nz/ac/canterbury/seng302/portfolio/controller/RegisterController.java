package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.Login;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;


/**
 * Controller class for the register page
 *
 * This page is responsible for allowing users to register a new account
 */
@Controller
public class RegisterController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

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

    @PostMapping("/register")
    public ModelAndView attemptRegistration(
            @ModelAttribute(name="registerForm") UserRequest registerRequest,
            Model model
    ) {
        //ToDo, use registerReply to update model
        UserRegisterResponse registerReply;
        try {
            // registerReply = userAccountsClientService.register(registerRequest);
            System.out.println(registerRequest.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView("redirect:/account");
    }

}