package nz.ac.canterbury.seng302.portfolio.controller.notifications;

import java.util.UUID;

/**
 * This class is used for an EditEvent object which is used by the notification classes.
 * The object has a user id associated with it, an eventId, a username, and the type of event.
 */
public class EditEvent {

    private int userId;
    private String eventId;
    private String usersName;
    private String typeOfEvent;
    private String nameOfEvent;

    public EditEvent(int userId, String usersName, String eventId, String nameOfEvent) {
        this.userId = userId;
        this.usersName = usersName;
        this.eventId = eventId;
        this.nameOfEvent = nameOfEvent;

    }

    public EditEvent(int userId, String usersName, String eventId, String typeOfEvent,  String nameOfEvent) {
        this.userId = userId;
        this.usersName = usersName;
        this.eventId = eventId;
        this.typeOfEvent = typeOfEvent;
        this.nameOfEvent = nameOfEvent;
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

    public void setUsersName(String usersName) {
        this.usersName = usersName;
    }

    public String getNameOfEvent() {
        return nameOfEvent;
    }

    public void setNameOfEvent(String nameOfEvent) {
        this.nameOfEvent = nameOfEvent;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUsersName(){
        return this.usersName;
    }
}
