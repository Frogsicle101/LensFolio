package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UploadController {

    @Autowired
    UserAccountsClientService userAccountsClientService;

    /**
     * Shows the form to upload a profile image if the user is logged in
     * @return The Thymeleaf upload html template.
     */
    @GetMapping("/upload")
    public String showUpload(
            @AuthenticationPrincipal AuthState principal,
            HttpServletRequest request,
            Model model
    ) {
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        String ip = request.getLocalAddr();
        String url = "http://" + ip + ":9001/" + user.getProfileImagePath();
        model.addAttribute("profileImageUrl", url);
        return "upload-image";
    }

}
