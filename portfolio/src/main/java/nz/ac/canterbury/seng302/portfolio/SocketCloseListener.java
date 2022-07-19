package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.OutgoingNotification;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.controller.notifications.NotificationService;
import nz.ac.canterbury.seng302.portfolio.controller.notifications.NotificationUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class handles websockets intentionally disconnecting or crashing. Sends a message to all connected clients to
 * inform that the client who disconnected is no longer editing anything.
 */
@Component
public class SocketCloseListener implements ApplicationListener<SessionDisconnectEvent> {

    /** Provides methods for sending STOMP messages */
    @Autowired
    private SimpMessagingTemplate template;

    /** For logging when disconnection events occur */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Notification service which provides the logic for sending notifications to subscribed users */
    private final static NotificationService notificationService = NotificationUtil.getNotificationService();


    /**
     * Spring publishes a SessionDisconnectEvent when a websocket goes down. This method listens for that event and
     * sends the STOMP message to all other clients.
     *
     * @param event The Spring SessionDisconnectionEvent
     */
    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        logger.info("Got SessionDisconnectEvent");
        Principal principal = event.getUser();
        PreAuthenticatedAuthenticationToken auth = (PreAuthenticatedAuthenticationToken) principal;
        if (auth != null) {

            AuthState state = (AuthState) auth.getPrincipal();
            String editorId = String.valueOf(PrincipalAttributes.getIdFromPrincipal(state));
            //Remove all the active notifications belonging to that user
            removeAndInform(editorId);
        } else {
            throw new RuntimeException("AuthState null in websocket disconnect message");
        }
    }

    /**
     * Helper method that removes all active notifications with the editor id given
     * and then informs the listeners subscribed to the notifications/sending/occasions endpoint
     * @param editorId The id of the person disconnected
     */
    private void removeAndInform(String editorId) {
        logger.info("User ID: " + editorId + " has disconnected, removing their active notifications.");

        List<OutgoingNotification> removedNotifications = notificationService.removeAllOutgoingNotificationByEditorId(editorId);
        ArrayList<OutgoingNotification> stopNotifications = new ArrayList<>();
        for (OutgoingNotification notification : removedNotifications) {
            stopNotifications.add( new OutgoingNotification(
                    editorId,
                    notification.getEditorName(),
                    notification.getOccasionType(),
                    notification.getOccasionId(),
                    "stop"
            ));
        }
        template.convertAndSend("notifications/sending/occasions", stopNotifications);
    }
}
