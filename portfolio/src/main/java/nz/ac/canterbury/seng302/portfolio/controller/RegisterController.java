package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import nz.ac.canterbury.seng302.portfolio.authentication.AuthenticationException;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Thymeleaf template for the register screen
     */
    @GetMapping("/register")
    public String register(Model model) {
        logger.info("GET REQUEST /register - get register page");
        return "accountRegister";
    }

    /**
     * Called when a user attempts to register a new account, if the registration is successful forwards a user to
     * their account page, otherwise informs the user why their attempt was unsuccessful.
     *
     * @param request - used to add a authentication cookie to successfully registered accounts
     * @param response - used to add a authentication cookie to successfully registered accounts
     * @param userRequest - A UserRequest object used to retrieve user input from the html.
     * @param model - Used to add attributes to the html page if needed.
     * @return view - the html page redirected to, either account details on successful registration or register on failure.
     */
    @PostMapping("/register")
    public ModelAndView attemptRegistration(
            HttpServletRequest request,
            HttpServletResponse response,
            @ModelAttribute(name="registerForm") UserRequest userRequest,
            Model model
    ) {
        logger.info("POST REQUEST /register - attempt to register new user");
        // Make UserRegisterRequest and send to Server
        UserRegisterResponse registerReply = userAccountsClientService.register(createUserRegisterRequest(userRequest));

        // Attempt to login new user
        if (registerReply.getIsSuccess()) {
            logger.info("Registration Success: " + registerReply.getMessage());
            logger.info("Log in new user");
            try {
                // NOTE: Successful login and cookie adding logged in LoginController
                AuthenticateResponse authenticateResponse = new LoginController()
                    .attemptLogin(userRequest,
                                request,
                                response,
                                authenticateClientService);
            } catch (AuthenticationException exception) {
                //Note: error logged when thrown
                model.addAttribute("errorMessage", exception.getMessage());
                return new ModelAndView("accountRegister");
            }
            return new ModelAndView("redirect:/account");
        }
        logger.info("Registration Failed: " + registerReply.getMessage());
        model.addAttribute("errorMessage", registerReply.getMessage());
        return new ModelAndView("accountRegister");
    }

    /**
     * Takes a UserRequest object populated from a registration form and returns a UserRegisterRequest to send to the server
     * 
     * @param userRequest - A UserRequest object populated from a accountRegister.html form
     * @return userRegisterRequest - a populated userRegisterRequest from the user_accounts.proto format
     */
    private UserRegisterRequest createUserRegisterRequest(UserRequest userRequest) {
        logger.info("Creating user register request from UserRequest");
        UserRegisterRequest.Builder userRegisterRequest = UserRegisterRequest.newBuilder();
        userRegisterRequest.setUsername(userRequest.getUsername())
                .setPassword(userRequest.getPassword())
                .setFirstName(userRequest.getFirstname())
                .setMiddleName(userRequest.getMiddlename())
                .setLastName(userRequest.getLastname())
                .setEmail(userRequest.getEmail())
                .setBio(userRequest.getBio())
                .setPersonalPronouns(userRequest.getPersonalPronouns())
                .setNickname(userRequest.getNickname());
        return userRegisterRequest.build();
    }
}