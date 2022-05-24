package nz.ac.canterbury.seng302.portfolio.DTO.STOMP;

/**
 * A Data Transfer Object (DTO) for sending notifications from the server to each client
 */
public class OutgoingNotification {
    private String editorName;
    private String occasionType;
    private String occasionId;
    private String action;

    /**
     * Constructor for OutgoingNotifications
     * @param editorName The name of the user making changes
     * @param occasionType The type of occasion we are editing
     * @param occasionId The ID of that occasion
     * @param action The action that has been performed. One of create, delete, edit, or stop.
     */
    public OutgoingNotification(String editorName, String occasionType, String occasionId, String action) {
        this.editorName = editorName;
        this.occasionType = occasionType;
        this.occasionId = occasionId;
        this.action = action;
    }

}
