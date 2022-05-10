package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



@Service
public class NotificationService {

    private final ConcurrentHashMap<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public HashMap<Integer, EditEvent> activeEdits = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public void addEmitter(Integer userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
    }


    public void removeEmitter(Integer userId) {
        emitters.remove(userId);
    }


    public void removeDeadEmitters(List<Integer> userIds) {
        for (int userId: userIds) {
            emitters.remove(userId);
        }
    }


    public SseEmitter initialiseEmitter (Integer userId) throws IOException {
        logger.info("Connected new notificationer");
        SseEmitter emitter = new SseEmitter();
        sendInitialEvents(emitter);
        emitter.onError(throwable -> {
            logger.warn("Emitter error, removed from emitters");
            removeEmitter(userId);
        });
        emitter.onTimeout(() -> {
            logger.warn("removing dead emitter");

            removeEmitter(userId);
        });
        addEmitter(userId, emitter);
        return emitter;
    }


    private void sendInitialEvents(SseEmitter emitter) throws IOException {
        if (activeEdits.isEmpty()) {
            logger.info("No initial edit events to send");
            emitter.send(SseEmitter.event().name("INIT"));
            return;
        }

        for (Map.Entry<Integer,EditEvent> actives : activeEdits.entrySet()) {
            emitter.send(SseEmitter.event().name("editEvent")
                    .data(actives.getValue()));
        }
        logger.info("Sent " + activeEdits.size() + " initial edit events");
    }


    public void sendNotification(String eventName, EditEvent editEvent) {
        logger.info("Sending Notification - " + eventName + " on " + editEvent.getEventId() + "[" + emitters.size() + " listeners]");
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


    private void updateActiveEdits(String eventName, EditEvent editEvent) {
        if (Objects.equals(eventName, "editEvent")) {
            activeEdits.put(editEvent.getUserId(), editEvent);
        } else if (Objects.equals(eventName, "notifyNotEditing")) {
            activeEdits.remove(editEvent.getUserId());
        } else {
            logger.warn("incorrect update event call");
        }
    }


    public void removeEditor(Integer userId) {
        EditEvent editEvent = activeEdits.get(userId);
        if (editEvent != null) {
            sendNotification("notifyNotEditing", editEvent);
            activeEdits.remove(editEvent.getUserId());
        }
    }
}
