package nz.ac.canterbury.seng302.portfolio.DTO;

/**
 * A data-transferal object for sending information about edits
 *
 * This objects models a JSON object that looks like
 * {
 *     "name": "Threderick"
 *     "subject": "Pizza Party"
 * }
 */
public class EditSTOMP {
    /**
     * The name of whoever is making edits.
     * This will be their first name followed by their last name.
     */
    private String name;
    /**
     * The name of whatever item the person is editing.
     */
    private String subject;

    /**
     * Constructor for EditSTOMP.
     * @param name The name of whoever is making edits.
     *     Please make this be their first name followed by their last name.
     * @param subject The name of whatever item the person is editing.
     *                No need to include the type.
     *                E.G. if the subject is a Deadline for 'The Very Important Report',
     *                just put in 'The Very Important Report'
     */
    public EditSTOMP(String name, String subject) {
        this.name = name;
        this.subject = subject;
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
}
