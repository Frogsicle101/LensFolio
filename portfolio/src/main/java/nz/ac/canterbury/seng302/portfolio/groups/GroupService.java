package nz.ac.canterbury.seng302.portfolio.groups;

import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
import org.springframework.stereotype.Service;

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
     * Adds a user to a group
     * @param groupId The id of the group
     * @param userId The id of the user
     */
    public void addUserToGroup(long groupId, int userId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException(userId + "does not refer to a valid user");
        }

        Group group = optionalGroup.get();
        group.addUserToGroup(userId);
        groupRepository.save(group);
    }

    /**
     * Removes a user from a group
     * @param groupId The id of the group
     * @param userId The id of the user
     */
    public void removeUserFromGroup(long groupId, int userId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException(userId + "does not refer to a valid user");
        } else if ( groupRepository.getGroupByGroupId(groupId).getMemberIds().contains(userId) == false) {
            throw new IllegalArgumentException(userId + " does not refer to a member of the given group");
        }


        Group group = optionalGroup.get();
        group.removeUserFromGroup(userId);
        groupRepository.save(group);
    }
}
