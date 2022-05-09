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
import java.util.Objects;
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


    @PostMapping("/notifyEdit")
    public void sendEventToClients(@AuthenticationPrincipal AuthState editor,
                                   @RequestParam UUID id) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                .setId(eventEditorID)
                .build());
        logger.info(id + " is being edited by user: " + eventEditorID);
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(id);
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


    @PostMapping("/notifyNotEditing")
    public void userCanceledEdit(
            @RequestParam(value="id") UUID id,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info(id + " is no longer being edited by user: " + eventEditorID);
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(id);
            editEvent.setUserId(eventEditorID);
            try {
                emitter.send(SseEmitter.event().name("userNotEditing")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/notifyReloadElement")
    public void reloadSpecificEvent(
            @RequestParam(value="id") UUID id,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info(id + " needs to be reloaded");
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(id);
            editEvent.setUserId(eventEditorID);
            try {
                emitter.send(SseEmitter.event().name("reloadElement")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/notifyRemoveElement")
    public void notifyRemoveEvent(
            @RequestParam(value="id") UUID id,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info(id + " needs to be removed");
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(id);
            editEvent.setUserId(eventEditorID);
            try {
                emitter.send(SseEmitter.event().name("notifyRemoveEvent")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    @PostMapping("/notifyNewElement")
    public void notifyNewEvent(
            @RequestParam(value="id") UUID id,
            @RequestParam(value="typeOfEvent") String typeOfEvent,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info(id + " needs to be added");
        logger.info(typeOfEvent);
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(id);
            editEvent.setUserId(eventEditorID);

            if (Objects.equals(typeOfEvent, "event")) {
                editEvent.setTypeOfEvent("event");
            } else if ("milestone".equals(typeOfEvent)) {
                editEvent.setTypeOfEvent("milestone");
            } //TODO add deadlines here

            try {
                emitter.send(SseEmitter.event().name("notifyNewElement")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }


}
