package nz.ac.canterbury.seng302.identityprovider.groups;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
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
     * Adds users to a group
     * @param groupId The id of the group
     * @param userIds The ids of the users
     */
    public void addUsersToGroup(Integer groupId, List<Integer> userIds) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        try {
            userRepository.findAllById(userIds);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }


        Group group = optionalGroup.get();
        group.addAllUsersToGroup(userIds);
        groupRepository.save(group);
    }

    /**
     * Removes a user from a group
     * @param groupId The id of the group
     * @param userId The id of the user
     */
    public void removeUserFromGroup(Integer groupId, int userId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        Optional<User> optionalUser = Optional.ofNullable(userRepository.findById(userId));
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException(userId + "does not refer to a valid user");
        } else if (!groupRepository.getGroupById(groupId).getMemberIds().contains(userId)) {
            throw new IllegalArgumentException(userId + " does not refer to a member of the given group");
        }


        Group group = optionalGroup.get();
        group.removeUserFromGroup(userId);
        groupRepository.save(group);
    }
}
