package nz.ac.canterbury.seng302.identityprovider.groups;

import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Provides utility functions to add and remove users from groups
 */
@Service
public class GroupService {

    private final GroupRepository repository;

    public GroupService(GroupRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds a user to a group
     * @param groupId The id of the group
     * @param userId The id of the user
     */
    public void addUserToGroup(Integer groupId, int userId) {
        Optional<Group> optionalGroup = repository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        Group group = optionalGroup.get();
        group.addUserToGroup(userId);
        repository.save(group);

    }

    /**
     * Removes a user from a group
     * @param groupId The id of the group
     * @param userId The id of the user
     */
    public void removeUserFromGroup(Integer groupId, int userId) {
        Optional<Group> optionalGroup = repository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        Group group = optionalGroup.get();
        group.removeUserFromGroup(userId);
        repository.save(group);

    }


}
