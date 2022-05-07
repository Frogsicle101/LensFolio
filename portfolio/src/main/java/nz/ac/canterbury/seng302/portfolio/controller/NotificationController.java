package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.EditEvent;
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

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


@RestController
public class NotificationController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());



    @CrossOrigin
    @GetMapping(value = "/notifications", consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribeToNotifications(@AuthenticationPrincipal AuthState principal) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        try {
            logger.info("Subscribing user: " + userId);
            emitter.send(SseEmitter.event().name("INIT"));
        } catch (IOException e) {
            logger.warn("Not subscribing users");
        }
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }


    @PostMapping("/eventEdit")
    public void sendEventToClients(@AuthenticationPrincipal AuthState editor,
                                   @RequestParam UUID eventId) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                .setId(eventEditorID)
                .build());
        logger.info("Event id " + eventId + " is being edited by user: " + eventEditorID);
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(eventId);
            editEvent.setUserId(eventEditorID);
            editEvent.setUserName(userResponse.getFirstName() + " " + userResponse.getLastName());

            try {
                emitter.send(SseEmitter.event().name("editEvent")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }


    @PostMapping("/userNotEditingEvent")
    public void userCanceledEdit(
            @RequestParam(value="eventId") UUID eventId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Event id " + eventId + " is no longer being edited by user: " + eventEditorID);
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(eventId);
            editEvent.setUserId(eventEditorID);
            try {
                emitter.send(SseEmitter.event().name("userNotEditingEvent")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/reloadSpecificEvent")
    public void reloadSpecificEvent(
            @RequestParam(value="eventId") UUID eventId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Event id " + eventId + " needs to be reloaded");
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(eventId);
            editEvent.setUserId(eventEditorID);
            try {
                emitter.send(SseEmitter.event().name("reloadSpecificEvent")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/notifyRemoveEvent")
    public void notifyRemoveEvent(
            @RequestParam(value="eventId") UUID eventId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Event id " + eventId + " needs to be removed");
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(eventId);
            editEvent.setUserId(eventEditorID);
            try {
                emitter.send(SseEmitter.event().name("notifyRemoveEvent")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/notifyNewEvent")
    public void notifyNewEvent(
            @RequestParam(value="eventId") UUID eventId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Event id " + eventId + " needs to be added");
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(eventId);
            editEvent.setUserId(eventEditorID);
            try {
                emitter.send(SseEmitter.event().name("notifyNewEvent")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }










    @PostMapping("/milestoneBeingEdited")
    public void sendMilestoneToClients(@AuthenticationPrincipal AuthState editor,
                                       @RequestParam UUID milestoneId) {
        int milestoneEditorId = PrincipalAttributes.getIdFromPrincipal(editor);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                .setId(milestoneEditorId)
                .build());
        logger.info("Milestone id " + milestoneId + " is being edited by user: " + milestoneEditorId);
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(milestoneId);
            editEvent.setUserId(milestoneEditorId);
            editEvent.setUserName(userResponse.getFirstName() + " " + userResponse.getLastName());

            try {
                emitter.send(SseEmitter.event().name("editMilestone")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }


    @PostMapping("/userNotEditingMilestone")
    public void userCanceledEditMilestone(
            @RequestParam(value="milestoneId") UUID milestoneId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int EditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Milestone id " + milestoneId + " is no longer being edited by user: " + EditorID);
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(milestoneId);
            editEvent.setUserId(EditorID);
            try {
                emitter.send(SseEmitter.event().name("userNotEditingMilestone")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/reloadSpecificMilestone")
    public void reloadSpecificMilestone(
            @RequestParam(value="milestoneId") UUID milestoneId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int editorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Milestone " + milestoneId + " needs to be reloaded");
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(milestoneId);
            editEvent.setUserId(editorID);
            try {
                emitter.send(SseEmitter.event().name("reloadSpecificMilestone")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/notifyRemoveMilestone")
    public void notifyRemoveMilestone(
            @RequestParam(value="milestoneId") UUID milestoneId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int editorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Milestone " + milestoneId + " needs to be removed");
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(milestoneId);
            editEvent.setUserId(editorID);
            try {
                emitter.send(SseEmitter.event().name("notifyRemoveMilestone")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/notifyNewMilestone")
    public void notifyNewMilestone(
            @RequestParam(value="milestoneId") UUID milestoneId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int editorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Milestone " + milestoneId + " needs to be added");
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(milestoneId);
            editEvent.setUserId(editorID);
            try {
                emitter.send(SseEmitter.event().name("notifyNewMilestone")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
