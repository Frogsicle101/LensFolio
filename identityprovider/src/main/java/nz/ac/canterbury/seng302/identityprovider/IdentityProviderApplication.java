package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.TestData.DataInitialisationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * The main IdP application class using springboot.
 */
@SpringBootApplication
public class IdentityProviderApplication {

    /** Checks if test data is required  */
    @Autowired
    DataInitialisationManager dataInitializer;

    /**
     * Initialises test data when the boolean variables are true
     */
    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
        dataInitializer.initialiseData();
    }


    /**
     * Main method see class documentation.
     *
     * @param args - default main params
     */
    public static void main(String[] args) {
        SpringApplication.run(IdentityProviderApplication.class, args);
    }

}
