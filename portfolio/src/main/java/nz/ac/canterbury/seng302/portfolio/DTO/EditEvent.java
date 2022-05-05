package nz.ac.canterbury.seng302.portfolio.DTO;

import java.util.UUID;

public class EditEvent {

    private int UserId;
    private UUID eventId;


    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }
}
