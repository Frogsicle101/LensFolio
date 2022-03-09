package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@SpringBootApplication
public class IdentityProviderApplication {

    @Autowired
    UserRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void setup() throws NoSuchAlgorithmException, InvalidKeySpecException {
        User testUser = new User(
                "steve",
                "password",
                "Steve",
                "McSteve",
                "Steveson",
                "Stev",
                "kdsflkdjf",
                "Steve/Steve",
                "steve@example.com"
        );
        testUser.addRole(UserRole.TEACHER);
        repository.save(testUser);

    }

    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }


}
