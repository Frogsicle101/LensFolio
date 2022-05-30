package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.IncomingNotification;
import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.OutgoingNotification;
import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.STOMPEditNotification;
import nz.ac.canterbury.seng302.portfolio.DTO.STOMP.STOMPOccasionMessage;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
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

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private DeadlineRepository deadlineRepository;


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
            @RequestParam(required = false) String id,
            @RequestParam String type,
            @RequestParam(required = false) String typeOfEvent
    ) throws IOException {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("POST /notifyEdit - edit type " + type + " on " + id + " by user : " + eventEditorID);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                .setId(eventEditorID)
                .build());

        String username = userResponse.getFirstName() + " " + userResponse.getLastName();
        if(Objects.equals(type, "keepAlive")) {
            notificationService.sendKeepAlive(eventEditorID);
        } else {
            if (typeOfEvent == null) {
                notificationService.sendNotification(type, new EditEvent(eventEditorID, username, id, typeOfEvent));
            } else {
                notificationService.sendNotification(type, new EditEvent(eventEditorID, username, id, typeOfEvent, getObjectName(typeOfEvent, id)));
            }

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


    public String getObjectName(String typeOfEvent, String id) {
        switch (typeOfEvent) {
            case "event" -> {
                Event event = eventRepository.getById(id);
                return event.getName();
            }
            case "milestone" -> {
                Milestone milestone = milestoneRepository.getById(id);
                return milestone.getName();
            }
            case "deadline" -> {
                Deadline deadline = deadlineRepository.getById(id);
                return deadline.getName();
            }
            default -> logger.warn("Notification Controller: getObjectName: Bad Request");
        }
        return null;
    }


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
        logger.info("Received message " + message.toString());
        /*
            Apparently, if you try and use @AuthenticationPrincipal in a @MessageMapping with anything other than a
            java.security.principal, it won't realise it's a principal, and will instead try to put the message in
            there.
            The current solution is we let it insert its java.security.principal, and then do some complicated
            typecasting to get it in the form we need it.

         */
        PreAuthenticatedAuthenticationToken auth = (PreAuthenticatedAuthenticationToken) principal;
        AuthState state = (AuthState) auth.getPrincipal();
        OutgoingNotification notification = new OutgoingNotification(state.getName(), message.getOccasionType(), message.getOccasionId(), message.getAction());
        //If we want to notify other users,
        logger.info("Received message " + message.getAction());
        if (Objects.equals(message.getAction(), "edit")) {
            notificationService.storeOutgoingNotification(notification);
        }
        return notification;
    }
}
