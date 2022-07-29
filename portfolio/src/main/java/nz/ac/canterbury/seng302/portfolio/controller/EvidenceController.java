package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * For creation of static evidence page. Feel free to delete if necessary
 */
@Controller
public class EvidenceController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * For requesting user information form the IdP.
     */
    @Autowired
    private UserAccountsClientService userAccountsClientService;

    @GetMapping("/evidence")
    public ModelAndView getEvidence(@AuthenticationPrincipal Authentication principal) {
        logger.info("GET REQUEST /groups - attempt to get all groups");

        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);

        ModelAndView modelAndView = new ModelAndView("evidence");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
