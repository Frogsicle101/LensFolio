package nz.ac.canterbury.seng302.portfolio.DTO.STOMP;

/**
 * A general purpose data-transferal object for STOMP messaging
 * For both to and from our server
 * This refers to occasions, so events deadlines and milestones.
 * Specifically, this is for making CRUD changes to an occasion.
 *
 * This objects models a JSON object that looks like
 * {
 *     type: create
 *     occasion: event
 *     subjectId: 12
 *     content: Gimme the content, Stanley.
 * }
 */
public class STOMPOccasionMessage {

    /**
     * What type of message this is.
     * Is it informing us to update something? delete something?
     * Or just notify people about something?
     * should be one of:
     * 'create', 'update', 'delete', 'notify'
     */
    private String type;
    /**
     * What occasion the edit is targeting.
     * Should be one of:
     * 'event', 'milestone', 'deadline'
     */
    private String occasion;
    /**
     * The ID of our subject
     */
    private int subjectId;
    /**
     * an optional content field, if we want to transmit any extra information
     * e.g. a message about who is editing what
     */
    private String content;

    /**
     * Constructor for the message.
     * This will leave the content field empty.
     * @param type What type of message this is.
     *      Is it informing us to update something? delete something?
     *      Or just notify people about something?
     *      should be one of:
     *      'create', 'update', 'delete', 'notify'
     * @param occasion What occasion the edit is targeting.
     *      Should be one of:
     *      'event', 'milestone', 'deadline'
     * @param subjectId The ID of our subject
     */
    public STOMPOccasionMessage(String type, String occasion, int subjectId) {
        this.type = type;
        this.occasion = occasion;
        this.subjectId = subjectId;
        this.content = "";
    }

    /**
     * Constructor for the message, with another argument to set the content
     * @param type What type of message this is.
     *      Is it informing us to update something? delete something?
     *      Or just notify people about something?
     *      should be one of:
     *      'create', 'update', 'delete', 'notify'
     * @param occasion What occasion the edit is targeting.
     *      Should be one of:
     *      'event', 'milestone', 'deadline'
     * @param subjectId The ID of our subject
     * @param content The content of our message, which can be just about anything
     */
    public STOMPOccasionMessage(String type, String occasion, int subjectId, String content) {
        this.type = type;
        this.occasion = occasion;
        this.subjectId = subjectId;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOccasion() {
        return occasion;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
