package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class UrlService {

    @Autowired
    private Environment env;

    public URL getProfileURL(User user) {

        String protocol = env.getProperty("protocol", "http");
        String hostName = env.getProperty("hostName", "localhost");
        int port = Integer.parseInt(env.getProperty("port", "9001"));
        String rootPath = env.getProperty("rootPath", "");

        try {
            return new URL(
                    protocol,
                    hostName,
                    port,
                    rootPath + user.getProfileImagePath()

            );
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL creation failed. Check application.properties has all required properties");
        }
    }
}
