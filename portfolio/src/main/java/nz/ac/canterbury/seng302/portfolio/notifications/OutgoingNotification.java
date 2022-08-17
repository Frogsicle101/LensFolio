package nz.ac.canterbury.seng302.portfolio.notifications;

/**
 * A Data Transfer Object (DTO) for sending notifications from the server to each client
 */
public class OutgoingNotification {

    /**
     * The ID of the person making the edit
     */
    private final String editorId;

    /**
     * The name of the editor (firstname lastname)
     */
    private final String editorName;

    /**
     * The subtype of occasion. One of 'event', 'milestone', or 'deadline'
     */
    private final String occasionType;

    /**
     * The ID of the edited occasion
     */
    private final String occasionId;

    /**
     * The type of message
     */
    private final String action;


    /**
     * Constructor for OutgoingNotifications
     *
     * @param editorId     The id of the user making changes
     * @param editorName   The name of the user making changes
     * @param occasionType The type of occasion we are editing
     * @param occasionId   The ID of that occasion
     * @param action       The action that has been performed. One of create, delete, edit, or stop.
     */
    public OutgoingNotification(String editorId, String editorName, String occasionType, String occasionId, String action) {
        this.editorId = editorId;
        this.editorName = editorName;
        this.occasionType = occasionType;
        this.occasionId = occasionId;
        this.action = action;
    }


    public String getEditorId() {
        return editorId;
    }


    public String getEditorName() {
        return editorName;
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
