package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
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

    /** For logging the requests related to groups. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


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
        logger.info("Adding users to group {}", groupId);
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            logger.info("Error adding users to group as group id is not valid");
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }
        Group group = optionalGroup.get();
        try {
            List<User> usersToAdd = (List<User>) userRepository.findAllById(userIds);
            group.addGroupMembers(usersToAdd);
            logger.info("Successfully added users to group {}", groupId);
            groupRepository.save(group);
        } catch (EntityNotFoundException e) {
            logger.info("Error adding users to group as user id is not valid");
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }
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
        logger.info("Removing users from group {}", groupId);
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            logger.info("Error removing users from group as group id is invalid");
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }
        Group group = optionalGroup.get();
        try {
            List<User> usersToRemove = (List<User>) userRepository.findAllById(userIds);
            group.removeGroupMembers(usersToRemove);
            logger.info("Successfully removed users from group {}", groupId);
            groupRepository.save(group);
        } catch (EntityNotFoundException e) {
            logger.info("Error removing users from group as user id is invalid");
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }
    }


    /**
     * Used to remove a user from a group when we only know the group shortname, useful for automatic removal when a
     * group object is not passed around (eg for auto removal from teacher group when teacher role is removed)
     *
     * @param shortname The shortname of the group the user is being removed from
     * @param userId The id of the user to be removed from the group
     */
    @Transactional
    public void removeGroupMembersByGroupShortName(String shortname, Integer userId) {
        logger.info("Retrieving group with shortname {}", shortname);
        Optional<Group> optionalGroup = groupRepository.findByShortName(shortname);
        if (optionalGroup.isPresent()) {
            int groupId = optionalGroup.get().getId();
            ArrayList<Integer> users = new ArrayList<>();
            users.add(userId);
            removeGroupMembers(groupId, users);
        }
    }


    /**
     * Used to add a user to a group when we only know the group shortname, useful for automatically adding a user when
     * we don't pass around a group object (eg for new users automatically being added to the Members Without A Group
     * group
     *
     * @param shortname The shortname of the group the user is being added to
     * @param userId The id of the user being added to the group
     */
    @Transactional
    public void addGroupMemberByGroupShortName(String shortname, Integer userId){
        logger.info("Retrieving group with shortname {}", shortname);
        Optional<Group> optionalGroup = groupRepository.findByShortName(shortname);
        if (optionalGroup.isPresent()) {
            int groupId = optionalGroup.get().getId();
            ArrayList<Integer> users = new ArrayList<>();
            users.add(userId);
            addGroupMembers(groupId, users);
        }
    }
}
