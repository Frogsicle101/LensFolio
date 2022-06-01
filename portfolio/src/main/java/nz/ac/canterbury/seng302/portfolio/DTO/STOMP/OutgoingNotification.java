package nz.ac.canterbury.seng302.portfolio.DTO.STOMP;

/**
 * A Data Transfer Object (DTO) for sending notifications from the server to each client
 */
public class OutgoingNotification {
    private String editorId;
    private String editorName;
    private String occasionType;
    private String occasionId;
    private String action;

    /**
     * Constructor for OutgoingNotifications
     * @param editorId The id of the user making changes
     * @param editorName The name of the user making changes
     * @param occasionType The type of occasion we are editing
     * @param occasionId The ID of that occasion
     * @param action The action that has been performed. One of create, delete, edit, or stop.
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

    public void setEditorId(String id) {
        this.editorId = id;
    }

    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }

    public String getOccasionType() {
        return occasionType;
    }

    public void setOccasionType(String occasionType) {
        this.occasionType = occasionType;
    }

    public String getOccasionId() {
        return occasionId;
    }

    public void setOccasionId(String occasionId) {
        this.occasionId = occasionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


}
