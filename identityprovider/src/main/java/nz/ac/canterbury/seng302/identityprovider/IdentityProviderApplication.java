package nz.ac.canterbury.seng302.identityprovider;

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
        repository.save(new User(
                "steve",
                "password",
                "Steve",
                "Steveson",
                "Stev",
                "kdsflkdjf",
                "Steve/Steve",
                "steve@example.com"
        ));

    }

    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }


}
