package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
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
     * Adds users to a group.
     *
     * @param groupId The id of the group.
     * @param userIds The ids of the users.
     * @throws IllegalArgumentException If the group ID or user IDs are invalid.
     */
    @Transactional
    public void addGroupMembers(Integer groupId, List<Integer> userIds) throws IllegalArgumentException{
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }
        Group group = optionalGroup.get();
        try {
            List<User> usersToAdd = (List<User>) userRepository.findAllById(userIds);
            group.addGroupMembers(usersToAdd);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }
        groupRepository.save(group);
    }


    /**
     * Removes users from a given group.
     *
     * @param groupId The id of the group from which users will be removed.
     * @param userIds The id of the users to be removed.
     * @throws IllegalArgumentException If the group ID or user IDs are invalid.
     */
    @Transactional
    public void removeGroupMembers(Integer groupId, List<Integer> userIds) throws IllegalArgumentException {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }
        Group group = optionalGroup.get();
        try {
            List<User> usersToRemove = (List<User>) userRepository.findAllById(userIds);
            group.removeGroupMembers(usersToRemove);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }

        groupRepository.save(group);
    }


    /**
     * Used to retrieve the teachers group id, this is used in order to add and remove users from this group on role
     * change
     *
     * @return Returns an Integer of the teacher group id or -1 if not found
     */
    public Integer getTeacherGroupId() {
        Optional<Group> group = groupRepository.findByShortName("Teachers");
        if (group.isPresent()) {
            return group.get().getId();
        } else {
            return -1;
        }
    }
}
