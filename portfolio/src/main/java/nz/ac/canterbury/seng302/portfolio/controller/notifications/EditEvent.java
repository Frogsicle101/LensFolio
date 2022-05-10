package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import java.util.UUID;

/**
 * This class is used for an EditEvent object which is used by the notification classes.
 * The object has a user id associated with it, an eventId, a username, and the type of event.
 */
public class EditEvent {

    private int userId;
    private UUID eventId;
    private String usersName;
    private String typeOfEvent;

    public EditEvent(int userId, String usersName, UUID eventId) {
        this.userId = userId;
        this.usersName = usersName;
        this.eventId = eventId;
    }

    public EditEvent(int userId, String usersName, UUID eventId, String typeOfEvent) {
        this.userId = userId;
        this.usersName = usersName;
        this.eventId = eventId;
        this.typeOfEvent = typeOfEvent;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserName(String userName){
        this.usersName = userName;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTypeOfEvent() {
        return typeOfEvent;
    }

    public void setTypeOfEvent(String typeOfEvent) {
        this.typeOfEvent = typeOfEvent;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getUsersName(){
        return this.usersName;
    }
}
