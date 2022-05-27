package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * A service class to assist the GroupsService class in updating groups and their memberships.
 */
public class GroupsServiceHelperService {

    /**
     * Verifies that a group exists and contains the users to be removed.
     *
     * @param groupId The group from which the users will be removed.
     * @param userIds The user IDs of the users to be removed from the given group.
     * @return The group, if it exists.
     * @throws IllegalArgumentException If the group or users do not exist.
     */
    static Group checkRequestValidity(Integer groupId, List<Integer> userIds, UserRepository userRepository, GroupRepository groupRepository) throws IllegalArgumentException {
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
