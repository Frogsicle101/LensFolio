package nz.ac.canterbury.seng302.identityprovider.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
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

    @RequestMapping("/profile/{name}.jpg")
    public void image(@PathVariable("name") String name, HttpServletResponse response) throws IOException {


        File image = new File("src/main/resources/profile-photos/" + name + ".jpg");

        if (!image.exists()) {
            image = new File("src/main/resources/profile-photos/default.png");
        }

        InputStream in = new FileInputStream(image);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        IOUtils.copy(in, response.getOutputStream());
        in.close();

    }

}
