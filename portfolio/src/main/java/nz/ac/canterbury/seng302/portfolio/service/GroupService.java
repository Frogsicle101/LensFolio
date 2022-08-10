package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A utility class for more complex actions involving groups, abstracted to make it more testable
 */
@Service
public class GroupService {

    /** The ID of the default teacher group */
    private static final int TEACHER_GROUP_ID = 1;

    /** Used to add / remove users from groups */
    private final GroupsClientService groupsClientService;

    /** Used to add and remove roles when users move groups */
    private final UserAccountsClientService userAccountsClientService;

    @Autowired
    public GroupService(GroupsClientService groupsClientService, UserAccountsClientService userAccountsClientService) {
        this.groupsClientService = groupsClientService;
        this.userAccountsClientService = userAccountsClientService;
    }


    /**
     * Add users to the given group, assigning the teacher role as needed
     *
     * @param groupId The group to add the users to
     * @param userIds The users to add to the group
     * @return A response message as defined in the protobuf
     */
    public AddGroupMembersResponse addUsersToGroup(int groupId, List<Integer> userIds) {
        if (groupId == TEACHER_GROUP_ID) {
            for (Integer userId: userIds) {
                ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                        .setRole(UserRole.TEACHER)
                        .setUserId(userId)
                        .build();
                userAccountsClientService.addRoleToUser(request);
            }
        }
        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserIds(userIds)
                .build();
        return groupsClientService.addGroupMembers(request);
    }


    /**
     * Removes users from the given group, assigning roles as needed
     * @param groupId The group to remove the users from
     * @param userIds The users to remove from the group
     * @return A response message as defined in the protobuf
     */
    public RemoveGroupMembersResponse removeUsersFromGroup(int groupId, List<Integer> userIds) {
        if (groupId == TEACHER_GROUP_ID) {
            for (Integer userId: userIds) {
                ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                        .setRole(UserRole.TEACHER)
                        .setUserId(userId)
                        .build();
                userAccountsClientService.removeRoleFromUser(request);
            }
        }
        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserIds(userIds)
                .build();
        return groupsClientService.removeGroupMembers(request);
    }
}
