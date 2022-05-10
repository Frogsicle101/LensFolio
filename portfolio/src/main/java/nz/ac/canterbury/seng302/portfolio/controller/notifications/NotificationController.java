package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


@RestController
public class NotificationController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    @Autowired NotificationService notificationService;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @CrossOrigin
    @GetMapping(value = "/notifications", consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribeToNotifications(@AuthenticationPrincipal AuthState principal,
                                               HttpServletResponse response) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        logger.info("GET /notifications - Subscribing user " + userId + " to notifications");
        try {
            SseEmitter emitter = notificationService.initialiseEmitter();
            response.addHeader("X-Accel-Buffering", "no");
            return emitter;
        } catch (IOException exception) {
            logger.warn("Failed to subscribe user to notifications - " + exception.getMessage());
            return null;
        }
    }


    @PostMapping("/notifyEdit")
    public void sendEventToClients(
            @AuthenticationPrincipal AuthState editor,
            @RequestParam UUID id,
            @RequestParam String type,
            @RequestParam(value="typeOfEvent", required = false) String typeOfEvent
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("POST /notifyEdit - edit type " + type + " on " + id + " by user : " + eventEditorID);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                .setId(eventEditorID)
                .build());
        String username = userResponse.getFirstName() + " " + userResponse.getLastName();
        if (!Objects.equals(type, "notifyNewElement")) {
            notificationService.sendNotification(type, new EditEvent(eventEditorID, username, id));
        } else {
            notificationService.sendNotification(type, new EditEvent(eventEditorID, username, id, typeOfEvent));
        }
    }
}
