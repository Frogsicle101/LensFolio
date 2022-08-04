package nz.ac.canterbury.seng302.portfolio.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nz.ac.canterbury.seng302.portfolio.CheckException;

import javax.persistence.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Represents an WebLink Entity
 */
@Entity
public class WebLink {

    @Id
    @GeneratedValue
    private int id;

    private String name;
    private URL url;
    private Boolean isSecured;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "evidence")
    private Evidence evidence;


    /**
     * Constructs an instance of the WebLink Object
     *
     * @param evidence The evidence that this weblink is associated with
     * @param name     the name of the weblink
     * @param url      the url of the weblink
     * @throws MalformedURLException when the url string is not valid. This Weblink is not allowed to be created.
     */
    public WebLink(Evidence evidence, String name, String url) throws MalformedURLException {
        if (name.length() > 20) {
            throw new CheckException("Name should be 20 characters or less");
        }
        if (name.length() <= 2) {
            throw new CheckException("Name should be longer than 1 character");
        }
        this.name = name;
        this.evidence = evidence;
        this.url = new URL(url);
        this.isSecured = Objects.equals(this.url.getProtocol(), "https");
    }

    /**
     * Default JPA Evidence constructor
     */
    public WebLink() {
    }

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public Boolean getIsSecured() {
        return isSecured;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public void setIsSecured(Boolean isSecured) {
        this.isSecured = isSecured;
    }


    public Evidence getEvidence() {
        return evidence;
    }

    /**
     * This method is used to help with testing. It returns the expected JSON string created for this object.
     *
     * @return the Json string the represents this piece of evidence.
     */
    public String toJsonString() {
        return "{" +
                "\"id\":" + id +
                ",\"name\":\"" + name + "\"" +
                ",\"url\":\"" + url +
                "\",\"isSecured\":" + isSecured +
                "}";
    }
}
