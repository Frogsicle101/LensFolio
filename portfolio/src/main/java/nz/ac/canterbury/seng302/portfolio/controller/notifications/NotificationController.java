package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.IncomingNotification;
import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.OutgoingNotification;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Controls sending and subscribing to event notifications, such as editing of events.
 *
 * This controller interacts with the Notification Service class which deals with the sending and subscribing functions
 */
@RestController
public class NotificationController {

    /** For logging */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Receives a IncomingNotification object that was sent to /notifications/receiving/message, creates a new
     * OutgoingNotification object, then sends it to /notifications/receiving/occasions.
     * All STOMP clients subscribed to that endpoint will receive the message.
     *
     * @param message A model for the edit details
     */
    @MessageMapping("/message")
    @SendTo("/notifications/sending/occasions")
    public OutgoingNotification receiveIncomingNotification(@AuthenticationPrincipal Principal principal, IncomingNotification message) {
        logger.info("Received " + message.getAction() + " message");

        // Spring's websocket handling doesn't support our AuthState type, so we typecast from java.security.Principal;
        PreAuthenticatedAuthenticationToken auth = (PreAuthenticatedAuthenticationToken) principal;
        AuthState state = (AuthState) auth.getPrincipal();
        String editorId = String.valueOf(PrincipalAttributes.getIdFromPrincipal(state));

        return new OutgoingNotification(editorId, state.getName(), message.getOccasionType(), message.getOccasionId(), message.getAction());
    }
}
