package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.shared.identityprovider.AddGroupMembersRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.AddGroupMembersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyRoleOfUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GroupService {

    private final int TEACHER_GROUP_ID = 0;

    private final GroupsClientService groupsClientService;
    private final UserAccountsClientService userAccountsClientService;

    @Autowired
    public GroupService(GroupsClientService groupsClientService, UserAccountsClientService userAccountsClientService) {
        this.groupsClientService = groupsClientService;
        this.userAccountsClientService = userAccountsClientService;
    }

    public AddGroupMembersResponse addUsersToGroup(int groupId, ArrayList<Integer> userIds) {

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
}
