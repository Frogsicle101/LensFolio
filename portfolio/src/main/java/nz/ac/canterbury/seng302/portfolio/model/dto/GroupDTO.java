package nz.ac.canterbury.seng302.portfolio.model.dto;

public class GroupDTO {

    private String shortName;
    private String longName;
    private int groupId;

    public GroupDTO(int groupId, String shortName, String longName){
        this.groupId = groupId;
        this.shortName = shortName;
        this.longName = longName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public int getGroupId() {
        return groupId;
    }
}
