package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class UploadController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    /**
     * Shows the form to upload a profile image if the user is logged in
     * @return The Thymeleaf upload html template.
     */
    @GetMapping("/uploadImage")
    public String showUpload(
            @AuthenticationPrincipal AuthState principal,
            HttpServletRequest request,
            Model model
    ) {
        logger.info("Endpoint reached: GET /uploadImage");
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        String ip = request.getLocalAddr();
        String url = "http://" + ip + ":9001/" + user.getProfileImagePath();
        model.addAttribute("profileImageUrl", url);
        return "upload-image";
    }

    @PostMapping("/upload")
    public String upload(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam("image") MultipartFile file,
            Model model
    ) throws IOException {
        logger.info("Endpoint reached: POST /upload");
        int id = PrincipalAttributes.getIdFromPrincipal(principal);
        userAccountsClientService.uploadProfilePhoto(file.getInputStream(), id, "jpg");
        return "upload-image";
    }

}
