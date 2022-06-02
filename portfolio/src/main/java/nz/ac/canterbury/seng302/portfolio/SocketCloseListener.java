package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.OutgoingNotification;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

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


    /**
     * Spring publishes a SessionDisconnectEvent when a websocket goes down. This method listens for that event and
     * sends the STOMP message to all other clients.
     *
     * @param event The Spring SessionDisconnectionEvent
     */
    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        logger.info("Got SessionDisconnectEvent" + event.getMessage() + "\n" + event.getUser());
        Principal principal = event.getUser();
        PreAuthenticatedAuthenticationToken auth = (PreAuthenticatedAuthenticationToken) principal;
        if (auth != null) {
            AuthState state = (AuthState) auth.getPrincipal();
            String editorId = String.valueOf(PrincipalAttributes.getIdFromPrincipal(state));

            template.convertAndSend("/notifications/sending/occasions",
                    new OutgoingNotification(
                            editorId,
                            state.getName(),
                            "*",
                            "*",
                            "stop"
                    )
            );
        } else {
            throw new RuntimeException("AuthState null in websocket disconnect message");
        }
    }
}
