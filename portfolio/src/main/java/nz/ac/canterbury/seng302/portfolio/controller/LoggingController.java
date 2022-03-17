package nz.ac.canterbury.seng302.portfolio.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;

public class LoggingController {

    static Logger logger = LoggerFactory.getLogger(LoggingController.class);

    public static void main(String[] args) {
        while(true) {
            logger.info("It works");

        }
    }
}
