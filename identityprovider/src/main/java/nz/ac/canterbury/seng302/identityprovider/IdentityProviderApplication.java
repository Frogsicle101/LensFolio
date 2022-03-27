package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.service.TimeService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class IdentityProviderApplication {

    @Autowired
    UserRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
        User testUser = new User(
                "steve",
                "password",
                "Steve",
                "McSteve",
                "Steveson",
                "Stev",
                "kdsflkdjf",
                "Steve/Steve",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        testUser.addRole(UserRole.TEACHER);
        repository.save(testUser);

        /*
        TODO: Remove this when finished testing
         */
        for (int i = 0; i < 10000; i++) {
            User lemming = new User(
                    "Lemming number: " + i,
                    "password",
                    "Steve",
                    "McSteve",
                    "Steveson",
                    "Stev",
                    "kdsflkdjf",
                    "Steve/Steve",
                    "steve@example.com",
                    TimeService.getTimeStamp()
            );
            repository.save(lemming);
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }


}
