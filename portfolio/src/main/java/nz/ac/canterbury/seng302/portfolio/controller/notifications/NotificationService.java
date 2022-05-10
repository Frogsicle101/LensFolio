package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Notification service provides the logic for sending event edit notifications to subscribed users.
 * <br>
 * Uses Spring Boot SseEmitters
 */
@Service
public class NotificationService {

    /** Hashmap which maps userId's to their notification emitter. */
    private final ConcurrentHashMap<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    /** Hashmap which maps userId's to the EditEvent they are making. */
    public HashMap<Integer, EditEvent> activeEdits = new HashMap<>();

    /** For logging */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Removes all the dead emitters from the emitters list.
     * <br>
     * @param userIds - A list of user Ids whose emitters are to be removed.
     */
    private void removeDeadEmitters(List<Integer> userIds) {
        logger.warn("Removed " + userIds.size() + " dead emitters");
        for (int userId: userIds) {
            emitters.remove(userId);
        }
    }


    /**
     * Creates a new SseEmitter for a subscribed client and sends them all the currently edited events
     * <br>
     * @param userId - The userId of the user needing the emitter
     * @return The newly created SseEmitter
     * @throws IOException - If the initial events fail to send.
     */
    public SseEmitter initialiseEmitter (Integer userId) throws IOException {
        logger.info("SERVICE - Connecting user " + userId + " to notifications");
        SseEmitter emitter = new SseEmitter();
        sendInitialEvents(emitter);
        emitter.onError(throwable -> {
            logger.warn("Emitter error, removed from emitters");
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            logger.warn("removing dead emitter");

            emitters.remove(userId);
        });
        emitters.put(userId, emitter);
        return emitter;
    }


    /**
     * Sends the initial event. I.e., the events being edited when the user originally subscribes.
     * <br>
     * @param emitter - the emitter to send the initial messages to.
     * @throws IOException - When a event message fails to send.
     */
    private void sendInitialEvents(SseEmitter emitter) throws IOException {
        if (activeEdits.isEmpty()) {
            emitter.send(SseEmitter.event().name("INIT"));
        } else {
            for (Map.Entry<Integer,EditEvent> actives : activeEdits.entrySet()) {
                emitter.send(SseEmitter.event().name("editEvent")
                        .data(actives.getValue()));
            }
        }
        logger.info("Sent " + activeEdits.size() + " initial edit events");
    }


    /**
     * Sends a notification to all subscribed users (emitters) containing the given eventName name and EditEvent data.
     * <br>
     * @param eventName - The name of the server sent event
     * @param editEvent - The data of the server sent event
     */
    public void sendNotification(String eventName, EditEvent editEvent) {
        logger.info("SERVICE Sending Notification - " + eventName + " on " + editEvent.getEventId() + "[" + emitters.size() + " listeners]");
        if (Objects.equals(eventName, "editEvent") || Objects.equals(eventName, "notifyNotEditing")) {
            updateActiveEdits(eventName, editEvent);
        }

        List<Integer> deadChannels = new ArrayList<>();
        for (Map.Entry<Integer,SseEmitter> emitterMap : emitters.entrySet()) {
            try {
                emitterMap.getValue().send(SseEmitter.event().name(eventName)
                                               .data(editEvent));
            } catch (Exception exception) {
                deadChannels.add(emitterMap.getKey());
            }
        }
        removeDeadEmitters(deadChannels);
    }


    /**
     * Updates the activeEdits list to notify users when they subscribe, that the event is being edited or not.
     * <br>
     * @param eventName - Either editEvent or notifyNotEditing for adding and removing from the list
     * @param editEvent - The edit event being added or removed form the list
     */
    private void updateActiveEdits(String eventName, EditEvent editEvent) {
        if (Objects.equals(eventName, "editEvent")) {
            activeEdits.put(editEvent.getUserId(), editEvent);
        } else if (Objects.equals(eventName, "notifyNotEditing")) {
            activeEdits.remove(editEvent.getUserId());
        } else {
            logger.warn("incorrect update event call");
        }
    }


    /**
     * Called when a user logs out, this removes their emitter and the event they are editing (if one exists)
     * <br>
     * @param userId - the id of the user to remove.
     */
    public void removeEditor(Integer userId) {
        logger.info("SERVICE - User " + userId + " unsubscribing and removing editEvent");
        EditEvent editEvent = activeEdits.get(userId);
        if (editEvent != null) {
            sendNotification("notifyNotEditing", editEvent);
            activeEdits.remove(editEvent.getUserId());
        }
    }
}
