package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.OutgoingNotification;
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

@Component
public class SocketCloseListener implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private SimpMessagingTemplate template;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        logger.info("Got SessionDisconnectEvent" + event.getMessage() + "\n" + event.getUser());
        Principal principal = event.getUser();
        PreAuthenticatedAuthenticationToken auth = (PreAuthenticatedAuthenticationToken) principal;
        if (auth != null) {
            AuthState state = (AuthState) auth.getPrincipal();

            template.convertAndSend("/notifications/sending/occasions",
                    new OutgoingNotification(
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
