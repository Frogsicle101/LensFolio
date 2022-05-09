package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
public class NotificationService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public SseEmitter initialiseEmitter () throws IOException {
        SseEmitter emitter = new SseEmitter();
        emitter.send(SseEmitter.event().name("INIT"));
        emitter.onCompletion(() -> removeEmitter(emitter));
        emitter.onError(throwable -> {
            logger.warn("Emitter error, removed from emitters");
            removeEmitter(emitter);
        });
        emitter.onTimeout(() -> {
            logger.warn("removing dead emitter");
            removeEmitter(emitter);
        });
        addEmitter(emitter);
        return emitter;
    }

    @Async
    public void sendNotification(String eventName, EditEvent editEvent) {
        logger.info("Sending Notification - " + eventName + " on " + editEvent.getEventId() + "[" + emitters.size() + " listeners]");
        List<SseEmitter> deadChannels = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name(eventName)
                                               .data(editEvent));
            } catch (Exception exception) {
                deadChannels.add(emitter);
            }
        });

        if (deadChannels.size() > 0) {
            logger.warn("Removing " + deadChannels.size() + " dead emitters");
            emitters.removeAll(deadChannels);
        }
    }
}
