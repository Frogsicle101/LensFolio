package nz.ac.canterbury.seng302.portfolio.evidence;

import nz.ac.canterbury.seng302.portfolio.CheckException;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Evidence entity
 */
@Entity
public class Evidence {

    @Id
    @GeneratedValue
    private int id;

    private int userId;
    private String title;
    private LocalDate date;
    private String description;


    @OneToMany(mappedBy = "evidence", fetch = FetchType.EAGER)
    private final List<WebLink> webLinks = new ArrayList<>();


    /**
     * Constructs an instance of the evidence object
     *
     * @param userId      the user associated with the evidence
     * @param title       the title of the evidence
     * @param date        the date of the evidence creation
     * @param description the description of the evidence
     */
    public Evidence(int userId, String title, LocalDate date, String description) {

        this.userId = userId;
        this.title = title;
        this.date = date;
        this.description = description;
    }


    /**
     * This constructor is used for testing only! Constructs an instance of the evidence object
     *
     * @param evidenceId  the ID of the evidence with is typically Generated automatically.
     * @param userId      the user associated with the evidence
     * @param title       the title of the evidence
     * @param date        the date of the evidence creation
     * @param description the description of the evidence
     */
    public Evidence(int evidenceId, int userId, String title, LocalDate date, String description) {
        if (title.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        }
        if (description.length() > 500) {
            throw new CheckException("description cannot be more than 500 characters");
        }
        this.id = evidenceId;
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

    /**
     * Verifies that the title is less than 500 characters, and sets the property if so.
     *
     * @param title The title to be checked and set.
     */
    public void setTitle(String title) {
        if (title.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        } else {
            this.title = title;
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public List<WebLink> getWebLinks() {
        return webLinks;
    }

    public void addWebLink(WebLink webLink) {
        this.webLinks.add(webLink);
    }

    public void addWebLinks(List<WebLink> webLinks) {
        this.webLinks.addAll(webLinks);
    }

    /**
     * Verifies that the description is less than 500 characters, and sets the property if so.
     *
     * @param description The description to be checked and set.
     */
    public void setDescription(String description) {
        if (description.length() > 500) {
            throw new CheckException("description cannot be more than 500 characters");
        } else {
            this.description = description;
        }
    }


    /**
     * This method is used to help with testing. It returns the expected JSON string created for this object.
     *
     * @return the Json string the represents this piece of evidence.
     */
    public String toJsonString() {
        return "{" +
                "\"id\":" + id +
                ",\"userId\":" + userId +
                ",\"title\":\"" + title +
                "\",\"date\":\"" + date +
                "\",\"description\":\"" + description +
                "\",\"webLinks\":" + "[]" +
                "}";
    }
}
