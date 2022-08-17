package nz.ac.canterbury.seng302.portfolio.model.domain.evidence;

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

    private String alias;
    private URL url;
    private Boolean isSecured;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "evidence")
    private Evidence evidence;


    /**
     * Constructs an instance of the WebLink Object
     *
     * @param evidence The evidence that this web link is associated with
     * @param alias     the name of the web link
     * @param url      the url of the web link
     * @throws MalformedURLException when the url string is not valid. This Weblink is not allowed to be created.
     */
    public WebLink(Evidence evidence, String alias, String url) throws MalformedURLException {
        if (alias.length() > 20) {
            throw new CheckException("Name should be 20 characters or less");
        }
        if (alias.length() < 1) {
            throw new CheckException("Name should be at least 1 character in length");
        }
        this.alias = alias;
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


    public String getAlias() {
        return alias;
    }

    public URL getUrl() {
        return url;
    }

    public Boolean getIsSecured() {
        return isSecured;
    }

    public void setAlias(String name) {
        this.alias = name;
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
                ",\"alias\":\"" + alias + "\"" +
                ",\"url\":\"" + url +
                "\",\"isSecured\":" + isSecured +
                "}";
    }
}
