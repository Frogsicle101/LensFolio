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
import java.util.Objects;
import java.util.UUID;

/**
 * Controls sending and subscribing to event notifications, such as editing of events.
 * <br>
 * This controller interacts with the Notification Service class which deals with the sending and subscribing functions
 */
@RestController
public class NotificationController {

    /** Used to get information about users who are making edits from the IdP */
    @Autowired
    private UserAccountsClientService userAccountsClientService;

    /** Notification service which provides the logic for sending notifications to subscribed users */
    @Autowired NotificationService notificationService;

    /** For logging */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Subscribes a user to receive notifications so that they know when events are being/has been edited
     * <br>
     * @param principal - The user who is requesting to subscribe to notifications
     * @param response - Used to clarify that send notifications should not be buffered.
     * @return the SseEmitter that the new subscriber uses
     */
    @CrossOrigin
    @GetMapping(value = "/notifications", consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribeToNotifications(@AuthenticationPrincipal AuthState principal,
                                               HttpServletResponse response) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        logger.info("GET /notifications - Subscribing user " + userId + " to notifications");
        try {
            // Logged in the service
            SseEmitter emitter = notificationService.initialiseEmitter(userId);
            response.addHeader("X-Accel-Buffering", "no");
            return emitter;
        } catch (IOException exception) {
            logger.warn("Failed to subscribe user to notifications - " + exception.getMessage());
            return null;
        }
    }


    /**
     * Sends an edit notification to all the subscribing users. This notification is determined by the parameters
     * of the request.
     * <br>
     * @param editor - The user who is making the edit, also the user sending the request.
     * @param id - the UUID of the event being edited
     * @param type - The type of edit, i.e., editEvent, no longer editing, event deleted etc
     * @param typeOfEvent - The type of the event being edited, not required, i.e., milestone, event, deadline
     */
    @PostMapping("/notifyEdit")
    public void sendEventToClients(
            @AuthenticationPrincipal AuthState editor,
            @RequestParam(required = false) UUID id,
            @RequestParam String type,
            @RequestParam(required = false) String typeOfEvent
    ) throws IOException {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("POST /notifyEdit - edit type " + type + " on " + id + " by user : " + eventEditorID);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                .setId(eventEditorID)
                .build());
        String username = userResponse.getFirstName() + " " + userResponse.getLastName();
        if (!Objects.equals(type, "notifyNewElement")) {
            notificationService.sendNotification(type, new EditEvent(eventEditorID, username, id));
        } else if(Objects.equals(type, "keepAlive")) {
            notificationService.sendKeepAlive(eventEditorID);
        } else {
            notificationService.sendNotification(type, new EditEvent(eventEditorID, username, id, typeOfEvent));
        }
    }


    /**
     * Removes an emitter of a subscribed user, also removes their events from the list of edited events.
     * <br>
     * @param user - The user who is no longer needing notifications and no longer editing
     */
    @PostMapping("/closeNotifications")
    public void closeNotifications(@AuthenticationPrincipal AuthState user) {
        int userId = PrincipalAttributes.getIdFromPrincipal(user);
        logger.info("POST /closeNotifications - cancelling notifications for user " + userId);
        notificationService.removeEditor(userId);
    }
}
