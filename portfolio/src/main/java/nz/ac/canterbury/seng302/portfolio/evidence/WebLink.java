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
    private Boolean secured; //True if its https, false if http

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="evidenceId")
    private Evidence evidence;

    /**
     * Constructs an instance of the WebLink Object
     *
     * @param name   the name of the weblink
     * @param url    the url of the weblink
     */
    public WebLink(String name, String url) throws MalformedURLException{
        if (name.length() > 20) {
            throw new CheckException("Name should be 20 characters or less");
        }
        if (name.length() <= 2) {
            throw new CheckException("Name should be longer than 1 character");
        }
        this.name = name;

        this.url = new URL(url);
        this.secured = Objects.equals(this.url.getProtocol(), "https");

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

    public Boolean getSecured() {
        return secured;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
    }
}
