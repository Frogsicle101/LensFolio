package nz.ac.canterbury.seng302.portfolio.DTO;

/**
 * A DTO for validating web links that contains their name and url
 */
public class ValidateWeblinkDTO {

    String url;
    String name;

    public ValidateWeblinkDTO(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
