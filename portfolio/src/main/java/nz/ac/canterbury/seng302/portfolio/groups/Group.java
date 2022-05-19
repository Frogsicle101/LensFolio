package nz.ac.canterbury.seng302.portfolio.groups;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;

/**
 * Object representation of a group of users
 */
@Entity
public class Group {

    @Id
    @GeneratedValue
    private Long id;

    @ElementCollection
    private ArrayList<Integer> memberIds;

    private String shortName;

    private String longName;

    protected Group() {}

    /**
     * Adds a user to the group object if the user is not already present
     * @param userId The id of the user
     */
    public void addUserToGroup(int userId) {
        if (!memberIds.contains(userId)) {
            memberIds.add(userId);
        }

    }

    /**
     * Removes a user from a group
     * @param userId the id of the user
     */
    public void removeUserFromGroup(int userId) {
        memberIds.remove((Integer) userId);
    }




}
