package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UploadController {

    /**
     * Shows the form to upload a profile image if the user is logged in
     * @return The Thymeleaf upload html template.
     */
    @GetMapping("/upload")
    public String showUpload() {
        return "upload-image";
    }

}
