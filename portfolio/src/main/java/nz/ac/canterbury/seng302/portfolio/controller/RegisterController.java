package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Controller class for the register page
 *
 * This page is responsible for allowing users to register a new account
 */
@Controller
public class RegisterController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;
    @Autowired
    private AuthenticateClientService authenticateClientService;

    /**
     *
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Thymeleaf template for the register screen
     */
    @GetMapping("/register")
    public String register(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public ModelAndView attemptRegistration(
            HttpServletRequest request,
            HttpServletResponse response,
            @ModelAttribute(name="registerForm") UserRequest registerRequest,
            Model model
    ) {
        //ToDo, clean this up with better names, maybe put in own method
        UserRegisterRequest.Builder registerRequest2 = UserRegisterRequest.newBuilder();
        registerRequest2.setUsername(registerRequest.getUsername())
                .setPassword(registerRequest.getPassword())
                .setFirstName(registerRequest.getFirstname())
                .setMiddleName(registerRequest.getMiddlename())
                .setLastName(registerRequest.getLastname())
                .setEmail(registerRequest.getEmail())
                .setBio(registerRequest.getBio())
                .setPersonalPronouns(registerRequest.getPersonalPronouns())
                .setNickname(registerRequest.getNickname());


        UserRegisterResponse registerReply = userAccountsClientService.register(registerRequest2.build());

        //todo check if this can be done in LoginController
        if (registerReply.getIsSuccess()) {
            AuthenticateResponse loginReply;
            //This try/catch block is the login attempt
            try {
                loginReply = authenticateClientService.authenticate(registerRequest.getUsername(), registerRequest.getPassword());
            } catch (StatusRuntimeException e){
                model.addAttribute("loginMessage", "Error connecting to Identity Provider...");
                return new ModelAndView("login");
            }
            //If the login was successful, create a cookie!
            if (loginReply.getSuccess()) {
                var domain = request.getHeader("host");
                CookieUtil.create(
                        response,
                        "lens-session-token",
                        loginReply.getToken(),
                        true,
                        5 * 60 * 60, // Expires in 5 hours
                        domain.startsWith("localhost") ? null : domain
                );
                return new ModelAndView("redirect:/account");
            }
        }

        model.addAttribute("errorMessage", registerReply.getMessage());
        return new ModelAndView("register");
    }

}