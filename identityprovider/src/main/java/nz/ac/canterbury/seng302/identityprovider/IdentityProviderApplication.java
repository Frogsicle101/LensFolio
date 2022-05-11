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

    private boolean includeAdminAccount = true;

    private boolean includeTestData = true;


    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
       if (includeTestData)
           addTestUsers();
       if (includeAdminAccount)
           addAdminAccount();
    }


    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }

    // ----------------------------------------- Test data ---------------------------------------------------

    private void addAdminAccount() {
        logger.info("Initialising Admin user");
        User admin = new User(
                "admin",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        admin.addRole(UserRole.COURSE_ADMINISTRATOR);
        repository.save(admin);
    }


    private void addTestUsers() {
        logger.info("Initialising test user Steve");
        User steve = new User(
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
        steve.addRole(UserRole.TEACHER);
        repository.save(steve);

        logger.info("Initialising test user Student");
        User student = new User(
                "student",
                "password",
                "Steve",
                "McSteveon",
                "Steveson",
                "Stev",
                "kdsflkdjf",
                "Steve/Steve",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        student.addRole(UserRole.STUDENT);
        repository.save(student);

        for (int i = 0; i < 1000; i++) {
            User lemming = new User(
                    "User " + i,
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
}
