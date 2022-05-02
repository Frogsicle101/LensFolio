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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.RegEx;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;


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
     * @return Thymeleaf template for the register screen
     */
    @GetMapping("/register")
    public String register() {
        logger.info("GET REQUEST /register - get register page");
        return "accountRegister";
    }

    /**
     * Called when a user attempts to register a new account, if the registration is successful forwards a user to
     * their account page, otherwise informs the user why their attempt was unsuccessful.
     *
     * @param userRequest - A UserRequest object used to retrieve user input from the html.
     * @return view - the html page redirected to, either account details on successful registration or register on failure.
     */
    @PostMapping("/register")
    public ResponseEntity<Object> attemptRegistration(
            @ModelAttribute(name="registerForm") UserRequest userRequest
    ) {
        logger.info("POST REQUEST /register - attempt to register new user");
        try{

            String firstname = userRequest.getFirstname();
            String middlename = userRequest.getMiddlename();
            String lastname = userRequest.getLastname();
            String username = userRequest.getUsername();
            String password = userRequest.getPassword();
            String email = userRequest.getEmail();
            String nickname = userRequest.getNickname();
            String pronouns = userRequest.getPersonalPronouns();
            String bio = userRequest.getBio();


            if (firstname == null // Checks that all necessary information is there.
                    || lastname == null
                    || username == null
                    || password == null
                    || email == null) {
                logger.warn("Registration Failed: Registration missing fields");
                return new ResponseEntity<>("Registration missing fields", HttpStatus.NOT_ACCEPTABLE);

            }


            String alphaSpacesRegex = "([a-zA-Z]+\s?)+"; // TODO pass this to the frontend, so we only need to change one set of REGEX expressions to effect both front-end/backend
            String userNameRegex = "([a-zA-Z0-9!#$%&'*+/=?^_`{|}~]+)";
            String emailRegex = "^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
            String bioRegex = "([a-zA-Z0-9.,'\"]+\\s?)+"; //TODO need to add bio regex into html, can't insert it directly as an attribute into the html tag so must do it with Jquery in the background.
            String passwordRegex = "([a-zA-Z0-9!#$%&'*+/=?^_`{|}~]+)"; // TODO, can someone review this, unsure about being able to check password, should it be hashed at this point?
            String pronounRegex = "([a-zA-Z/]+\\s?)+";

            //TODO should we break up the if statement below and have it check each thing individually so we can give individual feedback if something doesn't pass?

            // Checks that the strings passed through from the front-end are in formats that are acceptable with regex checks.
            if (!firstname.matches(alphaSpacesRegex)
                    || !lastname.matches(alphaSpacesRegex)
                    || !username.matches(userNameRegex)
                    || !email.matches(emailRegex)
                    || !password.matches(passwordRegex)
                    // Checks if the non-necessary fields have strings in them, if they do then they need to match the pattern that is acceptable.
                    || nickname != null && !nickname.matches(alphaSpacesRegex)
                    || middlename != null && !middlename.matches(alphaSpacesRegex)
                    || pronouns != null && !pronouns.matches(pronounRegex)
                    || bio != null && !bio.matches(bioRegex)) {
                logger.warn("Registration Failed: Registration field(s) not matching patterns");
                return new ResponseEntity<>("Registration field(s) not matching patterns",HttpStatus.NOT_ACCEPTABLE);
            }


            // Make UserRegisterRequest and send to Server
            UserRegisterResponse registerReply = userAccountsClientService.register(createUserRegisterRequest(userRequest));
            // Attempt to login new user
            if (registerReply.getIsSuccess()) {
                logger.info("Registration Success: {}", registerReply.getMessage());
                logger.info("Log in new user");
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                logger.info("Registration Failed: {}", registerReply.getMessage());
                return new ResponseEntity<>(registerReply.getMessage(), HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (Exception err) {
            logger.error("Registration Failed: {}",err.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }


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

    public void setUserAccountsClientService(UserAccountsClientService service) {
        this.userAccountsClientService = service;
    }



}