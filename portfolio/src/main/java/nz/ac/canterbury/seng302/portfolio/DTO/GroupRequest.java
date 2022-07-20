package nz.ac.canterbury.seng302.portfolio.DTO;

public class GroupRequest {

    private String shortName;
    private String longName;

    public GroupRequest(String shortName, String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }


    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }
}
