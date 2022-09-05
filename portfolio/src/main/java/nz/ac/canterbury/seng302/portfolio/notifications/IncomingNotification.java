package nz.ac.canterbury.seng302.portfolio.notifications;

/**
 * A Data Transfer Object (DTO) for clients to send messages about what they are doing to the server
 */
public class IncomingNotification {

    /**
     * The subtype of occasion or role being changed.
     */
    private final String occasionType;

    /**
     * The ID of the edited occasion or user whose roles have been updated
     */
    private final String occasionId;

    /**
     * The action that has been performed. One of create, delete, edit, stop, add role, or delete role.
     */
    private final String action;


    /**
     * Constructor for IncomingNotifications
     *
     * @param occasionType The type of occasion we are editing
     * @param occasionId   The ID of that occasion
     * @param action       The action that has been performed. One of create, delete, edit, stop, or roleChange.
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
