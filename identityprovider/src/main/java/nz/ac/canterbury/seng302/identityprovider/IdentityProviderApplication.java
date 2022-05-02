package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.service.TimeService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class IdentityProviderApplication {

    @Autowired
    UserRepository repository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
        logger.info("Initialising test user steve");
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
        logger.info("Initialising test user steve");
        User testUser1 = new User(
                "student",
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
        testUser.addRole(UserRole.STUDENT);
        repository.save(testUser1);

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
