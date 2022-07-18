package nz.ac.canterbury.seng302.identityprovider.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class ImageController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Environment env;

    @RequestMapping("/profile/{name}")
    public void image(@PathVariable("name") String name, HttpServletResponse response) throws IOException {
        logger.info("Retrieving profile image: " + name);

        String photoLocation = env.getProperty("photoLocation", "/src/main/resources/profile-photos/");

        File image = new File(photoLocation + name);


        if (!image.exists()) {
            logger.info("profile image does not exist using default image");
            image = new File(photoLocation + "default.png");
            System.out.println(image.getAbsolutePath());
        }

        InputStream in = new FileInputStream(image);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        IOUtils.copy(in, response.getOutputStream());
        in.close();
    }
}
