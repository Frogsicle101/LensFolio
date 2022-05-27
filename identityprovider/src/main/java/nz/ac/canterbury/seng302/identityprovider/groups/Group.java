package nz.ac.canterbury.seng302.identityprovider.groups;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a group of users.
 */
@Entity
@Table(name = "group_table") // had to add this, as I think Group can't be a table name in H2 as it's a reserved keyword?
public class Group {

    /**
    * The unique ID of the Group.
    */
    @Id
    private Integer id;

    /**
    * The ID's of the group's members.
    */
    @ElementCollection
    private List<Integer> memberIds;

    /**
    * The group's short name.
    */
    private String shortName;

    /**
    * The group's long name.
    */
    private String longName;


    /**
    * The Group constructor required by JPA.
    */
    protected Group() {}

    /**
    * The default constructor for a group, which automatically generates a unique ID.
    *
    * @param shortName The group's short name.
    * @param longName The group's long name.
    */
    public Group (String shortName, String longName) {
        this.shortName = shortName;
        this.longName = longName;
        this.memberIds = new ArrayList<>();
    }

    /**
     * The constructor for a group with a specified group ID.
     *
     * @param id The ID of the group to be created.
     * @param shortName The short name of the group to be created.
     * @param longName The long name of the group to be created.
     */
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

    public Integer getMembersNumber(){return getMemberIds().size();}

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Removes users from a group
     * @param userIds the id of the users to be removed
     */
    public void removeGroupMembers(List<Integer> userIds) {
        for (Integer userId : userIds)  {
            memberIds.remove(userId);
        }
    }

    /**
     * Adds a user to the group object if the user is not already present
     * @param userIds The ids of the users to be added
     */
    public void addGroupMembers(List<Integer> userIds) {
        for (Integer userId : userIds) {
            if (!memberIds.contains(userId)) {
                memberIds.add(userId);
            }
        }
    }
}
