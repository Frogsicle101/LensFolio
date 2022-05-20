package nz.ac.canterbury.seng302.portfolio.DTO.STOMP;

/**
 * A data-transferal object for sending information about edits
 * This will be sent from a client to the server.
 *
 * This objects models a JSON object that looks like
 * {
 *     "name": "Threderick"
 *     "subject": "Pizza Party"
 * }
 */
public class STOMPEditNotification {
    /**
     * The name of whoever is making edits.
     * This will be their first name followed by their last name.
     */
    private String name;
    /**
     * What occasion the edit is targeting.
     * Should be one of:
     * 'event', 'milestone', 'deadline'
     */
    private String occasion;
    /**
     * The name of whatever item the person is editing.
     */
    private String subject;
    /**
     * The ID of our subject
     */
    private int subjectId;
    /**
     * What type of message this is.
     * Is it informing us to update something? delete something?
     * Or just notify people about something?
     */
    private String type;

    /**
     * Constructor for STOMPEditNotification.
     * @param name The name of whoever is making edits.
     *      Please make this be their first name followed by their last name.
     * @param occasion The type of occasion we are editing
     * @param subject The name of what we're editing
     * @param subjectId The ID of what we're editing
     * @param type What type of message this is.
     *      Is it informing us to update something? delete something?
     *      Or just notify people about something?
     */
    public STOMPEditNotification(String name, String occasion, String subject, int subjectId, String type) {
        this.name = name;
        this.occasion = occasion;
        this.subject = subject;
        this.subjectId = subjectId;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
