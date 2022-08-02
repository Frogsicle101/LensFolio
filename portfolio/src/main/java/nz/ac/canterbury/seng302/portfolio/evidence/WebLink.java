package nz.ac.canterbury.seng302.portfolio.evidence;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import javax.persistence.*;

/**
 * Represents an WebLink Entity
 */
@Entity
public class WebLink {

    @Id
    @GeneratedValue
    private int id;

    private int userId;
    private String name;
    private String url;
    private Boolean secured; //True if its https, false if http

    @ManyToOne
    @JoinColumn(name="evidence_id")
    private Evidence evidence;

    /**
     * Constructs an instance of the WebLink Object
     *
     * @param userId the user associated with the weblink
     * @param name   the name of the weblink
     * @param url    the url of the weblink
     */
    public WebLink(int userId, String name, String url) {
        if (name.length() > 20) {
            throw new CheckException("Name should be 20 characters or less");
        }
        if (name.length() <= 2) {
            throw new CheckException("Name should be longer than 1 character");
        }
        this.userId = userId;
        this.name = name;
        this.url = url;
    }

    /**
     * Default JPA Evidence constructor
     */
    public WebLink() {
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getSecured() {
        return secured;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
    }
}
