package nz.ac.canterbury.seng302.portfolio.DTO.STOMP;

/**
 * A Data Transfer Object (DTO) for clients to send messages about what they are doing to the server
 */
public class IncomingNotification {

    /** The subtype of occasion. One of 'event', 'milestone', or 'deadline' */
    private final String occasionType;

    /** The ID of the edited occasion */
    private final String occasionId;

    /** The ID of the person making the edit */
    private final String action;


    /**
     * Constructor for IncomingNotifications
     * @param occasionType The type of occasion we are editing
     * @param occasionId The ID of that occasion
     * @param action The action that has been performed. One of create, delete, edit, or stop.
     */
    public IncomingNotification(String occasionType, String occasionId, String action) {
        this.occasionType = occasionType;
        this.occasionId = occasionId;
        this.action = action;
    }


    public String getOccasionType() {
        return occasionType;
    }


    public String getOccasionId() {
        return occasionId;
    }


    public String getAction() {
        return action;
    }

}
