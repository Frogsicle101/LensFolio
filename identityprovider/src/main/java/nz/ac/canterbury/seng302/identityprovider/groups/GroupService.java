package nz.ac.canterbury.seng302.identityprovider.groups;

import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * Provides utility functions to add and remove users from groups
 */
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    /**
     * Adds users to a group.
     *
     * @param groupId The id of the group
     * @param userIds The ids of the users
     */
    public void addUsersToGroup(Integer groupId, List<Integer> userIds) {
        Group group = checkRequestValidity(groupId, userIds);
        group.addAllUsersToGroup(userIds);
        groupRepository.save(group);
    }

    /**
     * Removes users from a given group.
     *
     * @param groupId The id of the group from which users will be removed.
     * @param userIds The id of the users to be removed.
     */
    public void removeUsersFromGroup(Integer groupId, List<Integer> userIds) throws IllegalArgumentException {
        Group group = checkRequestValidity(groupId, userIds);
        group.removeAllUsersFromGroup(userIds);
        groupRepository.save(group);
    }

    /**
     * Verifies that a group exists and contains the users to be removed.
     *
     * @param groupId The group from which the users will be removed.
     * @param userIds The user IDs of the users to be removed from the given group.
     * @return The group, if it exists.
     * @throws IllegalArgumentException If the group or users do not exist.
     */
    private Group checkRequestValidity(Integer groupId, List<Integer> userIds) throws IllegalArgumentException {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        try {
            userRepository.findAllById(userIds);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }
        return optionalGroup.get();
    }
}
