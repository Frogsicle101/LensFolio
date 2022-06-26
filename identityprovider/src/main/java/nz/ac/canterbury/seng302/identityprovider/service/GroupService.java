package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Provides utility functions to add and remove users from groups.
 */
@Service
public class GroupService {

    /** The repository containing the groups being managed by the group service. */
    private final GroupRepository groupRepository;

    /** The repository containing the users being managed by the group service. */
    private final UserRepository userRepository;


    /**
     * The default constructor for the group service.
     *
     * @param groupRepository The repository containing the groups being managed by the group service.
     * @param userRepository The repository containing the users being managed by the group service.
     */
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }


    /**
     * Adds users to a group. If the group is the Members Without A Group group, the users will be removed from every
     * other group. Otherwise, removes the users from Members Without A Group if they are a member
     *
     * @param groupId The id of the group.
     * @param userIds The ids of the users.
     * @throws Exception If the group ID or user IDs are invalid.
     */
    @Transactional
    public void addGroupMembers(Integer groupId, List<Integer> userIds) throws Exception {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }
        Group group = optionalGroup.get();
        try {
            List<User> usersToAdd = (List<User>) userRepository.findAllById(userIds);
            if (group.getLongName().equals("Members Without A Group")) {
                addUsersToMWAG(usersToAdd, group); // Need to remove users from all the other groups in this case
            } else {
                Group MwagGroup = getMWAG();
                for (User user : usersToAdd) {
                    group.addGroupMember(user);
                    if (user.getGroups().contains(MwagGroup)) {
                        removeUserFromMWAG(user, MwagGroup);
                    }
                }
            }
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }
        groupRepository.save(group);
    }


    /**
     * Removes users from a given group. Checks if the user has been removed from their last group and if so, adds them
     * to Members Without A Group
     *
     * @param groupId The id of the group from which users will be removed.
     * @param userIds The id of the users to be removed.
     * @throws Exception If the group ID or user IDs are invalid.
     */
    @Transactional
    public void removeGroupMembers(Integer groupId, List<Integer> userIds) throws Exception {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }
        Group group = optionalGroup.get();
        if (group.getLongName().equals("Members Without A Group")){
            throw new IllegalArgumentException("Can't remove user from 'Members Without A Group'");
        }
        try {
            List<User> usersToRemove = (List<User>) userRepository.findAllById(userIds);
            for (User user : usersToRemove){
                group.removeGroupMember(user);
                checkIfUserInNoGroup(user);
            }
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }

        groupRepository.save(group);
    }


    /**
     * Used to retrieve group id, this is used by other classes so that they can add and remove users from groups
     *
     * @return Returns an Integer of the teacher group id or -1 if not found
     */
    public Integer getGroupIdByShortName(String groupShortName) {
        Optional<Group> group = groupRepository.findByShortName(groupShortName);
        if (group.isPresent()) {
            return group.get().getId();
        } else {
            return -1;
        }
    }


    /**
     * Removes to user from all the groups they are currently a member of
     *
     * @param user user to be removed from all groups
     */
    private void removeUserFromAllGroups(User user){
        List<Group> usersCurrentGroups = user.getGroups();
        for (Group group: usersCurrentGroups){
            group.removeGroupMember(user);
        }
    }


    /**
     * Adds the users to Members Without A Group, also removes them from every other group
     *
     * @param usersToAdd a list of users the be added to Members Without A Group
     * @param MwagGroup The Members Without A Group group to add the users to
     */
    private void addUsersToMWAG(List<User> usersToAdd, Group MwagGroup) {
        for (User user: usersToAdd) {
            removeUserFromAllGroups(user);
            MwagGroup.addGroupMember(user);
        }
    }


    /**
     * Checks if the user is not part of any group and if so, adds them to Members Without A Group
     *
     * @param user The user to check
     * @throws Exception Thrown when there is an error getting Members Without A Group from the repository
     */
    private void checkIfUserInNoGroup(User user) throws Exception {
        Group group = getMWAG();
        if (group == (null)) {
            throw new Exception("An error occurred getting the MWAG group");
        } else {
            if (user.getGroups().size() == 0) { // user in no other groups
                group.addGroupMember(user);
            }
        }
    }


    /**
     * Removes the user from Members Without A Group
     *
     * @param user The user to remove
     * @throws Exception Thrown when there is an error getting Members Without A Group from the repository
     */
    private void removeUserFromMWAG(User user, Group MwagGroup) throws Exception {
        if (MwagGroup == (null)){
            throw new Exception("An error occurred getting the MWAG group");
        } else {
            MwagGroup.removeGroupMember(user);
        }
    }


    /**
     * Gets the Member Without A Group from the group repository
     *
     * @return The Member Without A Group group
     */
    public Group getMWAG() {
        Optional<Group> group = groupRepository.findByShortName("Non-Group");
        return group.orElse(null);
    }
}