package nz.ac.canterbury.seng302.portfolio.controller.notifications;
import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.OutgoingNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;


/**
 * Notification service provides the logic for sending event edit notifications to subscribed users.
 *
 *
 */
@Service
public class NotificationService {

    /** For logging */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Hashmap which stores the currently active edit notifications
     * Keys should be stored as a string of the format:
     * occasionType + ":" + occasionId
     * where the type and id are taken from the notification stored.
     */
    private HashMap<String, OutgoingNotification> activeEditNotifications = new HashMap<>();

    /**
     * Stores the outgoing notification, to be later sent to other users.
     * This should only be done for actions where we need to handle its 'in-progress' state
     * e.g. an edit action.
     * Something like a delete action would NOT need to be stored, because its effects
     * happen (more or less) instantaneously.
     * @param notification The notification to be stored. Must have a type and ID.
     */
    public void storeOutgoingNotification(OutgoingNotification notification) {
        String key = notification.getOccasionType() + ":" + notification.getOccasionId();
        logger.info("SERVICE - Storing notification: " + key);
        activeEditNotifications.put(key, notification);
    }

    public Collection<OutgoingNotification> sendStoredNotifications() {
        logger.info("SERVICE - SENDING STORED NOTIFICATIONS");
        return activeEditNotifications.values();
    }

}
