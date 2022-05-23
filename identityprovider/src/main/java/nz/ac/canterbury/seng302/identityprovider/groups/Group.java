package nz.ac.canterbury.seng302.identityprovider.groups;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a group of users
 */
@Entity
public class Group {

    @Id
    private Integer id;

    @ElementCollection
    private List<Integer> memberIds;

    private String shortName;

    private String longName;

    protected Group() {}

    public Group (String shortName, String longName) {
        this.shortName = shortName;
        this.longName = longName;
        this.memberIds = new ArrayList<>();
    }

    public Group (Integer id, String shortName, String longName) {
        this.id = id;
        this.shortName = shortName;
        this.longName = longName;
        this.memberIds = new ArrayList<>();
    }


    public Integer getId() {
        return id;
    }

    public List<Integer> getMemberIds() {
        return memberIds;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }


    /**
     * Removes a user from a group
     * @param userId the id of the user
     */
    public void removeUserFromGroup(int userId) {
        memberIds.remove((Integer) userId);
    }

    /**
     * Adds a user to the group object if the user is not already present
     * @param userIds The ids of the users to be added
     */
    public void addAllUsersToGroup(List<Integer> userIds) {
        for (Integer userId : userIds) {
            if (!memberIds.contains(userId)) {
                memberIds.add(userId);
            }
        }
    }
}
