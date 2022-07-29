package nz.ac.canterbury.seng302.portfolio.evidence;

import nz.ac.canterbury.seng302.portfolio.CheckException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * Represents a Evidence entity
 */
@Entity
public class Evidence {

    @Id
    @GeneratedValue
    private int id;

    private int userId;
    private String title;
    private LocalDateTime date;
    private String description;

    /**
     * Constructs an instance of the evidence object
     * @param userId the user associated with the evidence
     * @param title the title of the evidence
     * @param date the date of the evidence creation
     * @param description the description of the evidence
     */
    public Evidence(int userId, String title, LocalDateTime date, String description) {
        if (title.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        }
        if (description.length() > 500) {
            throw new CheckException("description cannot be more than 500 characters");
        }
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.description = description;

    }

    /**
     * Default JPA Evidence constructor
     */
    public Evidence() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        } else {
            this.title = title;
        }

    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        if (description.length() > 500) {
            throw new CheckException("description cannot be more than 500 characters");
        } else {
            return description;
        }

    }

    public void setDescription(String description) {
        this.description = description;
    }
}
