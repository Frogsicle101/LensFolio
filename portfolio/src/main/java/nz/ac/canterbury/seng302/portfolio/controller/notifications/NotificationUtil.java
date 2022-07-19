package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

    private final static NotificationService notificationService = new NotificationService();

    @Autowired
    private NotificationUtil() {
        super();
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }

}
