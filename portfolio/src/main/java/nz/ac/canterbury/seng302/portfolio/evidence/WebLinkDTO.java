package nz.ac.canterbury.seng302.portfolio.evidence;

/**
 * Weblink Data Transfer Object, used for representing each item in the list of weblinks that can come after submitting
 * a post to the evidence endpoint.
 */
public class WebLinkDTO {

    /** The readable name of the link */
    private String name;

    /** The actual followable URL */
    private String url;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public WebLinkDTO(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
