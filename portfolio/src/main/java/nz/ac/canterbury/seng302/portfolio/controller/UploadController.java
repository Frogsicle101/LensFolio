package nz.ac.canterbury.seng302.portfolio.controller;

import com.google.rpc.context.AttributeContext;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
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

    @PostMapping("/upload")
    public String upload(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam("image") MultipartFile file
    ) throws IOException {
        int id = PrincipalAttributes.getId(principal);
        userAccountsClientService.uploadProfilePhoto(file.getInputStream(), id, "jpg");
        return "upload-image";
    }

    @DeleteMapping("/deleteProfileImg")
    public void delete(
            @AuthenticationPrincipal AuthState principal
    ) {
        int id = PrincipalAttributes.getId(principal);

        DeleteUserProfilePhotoRequest deleteRequest = DeleteUserProfilePhotoRequest.newBuilder().setUserId(id).build();

        userAccountsClientService.deleteUserProfilePhoto(deleteRequest);
    }


}
