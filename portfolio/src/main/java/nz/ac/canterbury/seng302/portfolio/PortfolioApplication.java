package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.controller.LoggingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortfolioApplication {

    static Logger logger = LoggerFactory.getLogger(LoggingController.class);

    public static void main(String[] args) {


        SpringApplication.run(PortfolioApplication.class, args);
        logger.error("Eureka!");
    }

}
