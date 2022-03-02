package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.service.DatabaseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.sql.SQLException;

@SpringBootApplication
public class IdentityProviderApplication {

    @EventListener(ApplicationReadyEvent.class)
    public void setup() throws SQLException {
        DatabaseService.setUpDatabase();

    }

    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }



}
