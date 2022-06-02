package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.IncomingNotification;
import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.OutgoingNotification;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Objects;

/**
 * Controls sending and subscribing to event notifications, such as editing of events.
 *
 * This controller interacts with the Notification Service class which deals with the sending and subscribing functions
 */
@RestController
public class NotificationController {

    /** Notification service which provides the logic for sending notifications to subscribed users */
    @Autowired NotificationService notificationService;

    /** For logging */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());



    /**
     * A message-mapping method that will:
     * receive a IncomingNotification object that was sent to /notifications/receiving/message
     * (the /notifications/sending part is pre-configured over in the WebSocketConfig class)
     * Make a string that will be the content of our editing notification
     * Put it into a OutgoingNotification object
     * Send it off to /notifications/receiving/occasions, for any and all STOMP clients subscribed to that endpoint
     *     *
     * Don't call this method directly. This is a spring method; it'll call itself when the time is right.
     * @param message A model for the edit details
     * @return A messenger object containing a type, occasion, id and content
     */
    @MessageMapping("/message")
    @SendTo("/notifications/sending/occasions")
    public OutgoingNotification receiveIncomingNotification(@AuthenticationPrincipal Principal principal, IncomingNotification message) {
        logger.info("Received " + message.getAction() + " message");

        // Spring's websocket handling doesn't support our AuthState type, so we typecast from java.security.Principal;
        PreAuthenticatedAuthenticationToken auth = (PreAuthenticatedAuthenticationToken) principal;
        AuthState state = (AuthState) auth.getPrincipal();
        String editorId = String.valueOf(PrincipalAttributes.getIdFromPrincipal(state));
        OutgoingNotification notification = new OutgoingNotification(state.getName(), message.getOccasionType(), message.getOccasionId(), message.getAction());
        //If we want to notify other users,
        logger.info("Received message " + message.getAction());
        if (Objects.equals(message.getAction(), "edit")) {
            notificationService.storeOutgoingNotification(notification);
        }
        return notification;
    }
}
