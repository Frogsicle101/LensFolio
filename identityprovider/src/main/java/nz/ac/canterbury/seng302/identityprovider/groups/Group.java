package nz.ac.canterbury.seng302.identityprovider.groups;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.service.UserHelperService;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a group of users.
 */
@Entity
@Table(name = "group_table") // had to add this, as I think Group can't be a table name in H2 as it's a reserved keyword?
public class Group {

    /** The unique ID of the Group. */
    @Id
    private Integer id;

    /**
    * The ID's of the group's members.
    */
    @ManyToMany
    @JoinTable(name = "group_members",
            joinColumns = @JoinColumn(name = "groupId"),
            inverseJoinColumns = @JoinColumn(name = "userId"))
    private List<User> userList = new ArrayList<>();

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
    }


    public Integer getId() {
        return id;
    }

    public List<User> getUserList() {
        return this.userList;
    }

    public Integer getMembersNumber(){return this.userList.size();}

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
    public void removeGroupMembers(List<User> users) {
        for (User user : users)  {
            userList.remove(user);
        }
    }

    /**
     * Adds a user to the group object if the user is not already present
     * @param userIds The ids of the users to be added
     */
    public void addGroupMembers(List<User> users) {
        for (User user : users) {
            if (!userList.contains(user)) {
                userList.add(user);
            }
        }
    }


    /**
     * Converts this group to a GroupDetailsResponse
     *
     * @return GroupDetailsResponse - the GroupDetailsResponse equivalent of this group
     */
    public GroupDetailsResponse groupDetailsResponse() {
        GroupDetailsResponse.Builder response = GroupDetailsResponse.newBuilder()
                .setLongName(this.getLongName())
                .setShortName(this.getShortName())
                .setGroupId(this.getId());
        List<User> groupMembers = this.getUserList();
        for (User user : groupMembers) {
            //For each group member ID that the group has, we want to create a UserResponse.
            response.addMembers(user.userResponse());
        }

        return response.build();
    }


}
