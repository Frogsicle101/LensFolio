package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

/**
 * The controller for managing requests to edit groups and their user's memberships.
 */
@Controller
public class GroupsController {

    /**
     * For logging the requests related to groups.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * For making gRpc requests to the IdP.
     */
    @Autowired
    private GroupsClientService groupsClientService;


    /**
     * Restricted to teachers and course administrators, This endpoint deletes an existing group.
     *
     * @param principal The user who made the request.
     * @param groupId   The group ID of the group to be deleted.
     * @return ResponseEntity A response entity containing either OK or NOT FOUND (for now).
     */
    @DeleteMapping("/groups/edit")
    public ResponseEntity<String> deleteGroup(@AuthenticationPrincipal Authentication principal,
                                              @RequestParam Integer groupId) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("DELETE REQUEST /groups - attempt to delete group {} by user: {}", groupId, userId);
        try {
            DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                    .setGroupId(groupId)
                    .build();
            DeleteGroupResponse response = groupsClientService.deleteGroup(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("ERROR /groups/edit - an error occurred while deleting a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Restricted to teachers and course administrators, This endpoint creates a new group.
     *
     * @param principal The user who made the request.
     * @param shortName The short name of the group.
     * @param longName  The full name of the group.
     * @return ResponseEntity A response entity containing either CREATED or BAD_REQUEST (for now).
     */
    @PostMapping("/groups/edit")
    public ResponseEntity<String> createGroup(@AuthenticationPrincipal Authentication principal,
                                              @RequestParam String shortName,
                                              @RequestParam String longName) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("POST REQUEST /groups/edit - attempt to create group {} by user: {}", shortName, userId);
        try {
            CreateGroupRequest request = CreateGroupRequest.newBuilder()
                    .setShortName(shortName)
                    .setLongName(longName)
                    .build();
            CreateGroupResponse response = groupsClientService.createGroup(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/edit - an error occurred while creating a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Restricted to teachers and course administrators, This endpoint modify a group details.
     *
     * @param principal The user who made the request.
     * @param shortName The new short name of the group.
     * @param longName  The new long name of the group.
     * @return ResponseEntity A response entity containing either Modified or BAD_REQUEST (for now).
     */
    @PostMapping("/groups/edit/details")
    public ResponseEntity<String> modifyGroupDetails (@AuthenticationPrincipal Authentication principal,
                                                      @RequestParam Integer groupId,
                                                      @RequestParam String shortName,
                                                      @RequestParam String longName) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("POST REQUEST /groups/edit/details - attempt to modify details of group {} by user: {}",groupId, userId);
        try {
            ModifyGroupDetailsRequest request = ModifyGroupDetailsRequest.newBuilder()
                    .setGroupId(groupId)
                    .setShortName(shortName)
                    .setLongName(longName)
                    .build();
            ModifyGroupDetailsResponse response = groupsClientService.modifyGroupDetails(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/edit/details - an error occurred while modify a group details");
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Post mapping for a user to be added to a group. Restricted to course administrators and teachers.
     *
     * @param userIds The users to be added to the group.
     * @param groupId The group to which the use will be added.
     * @return A response entity containing the status of the response and the response message.
     */
    @PostMapping("/groups/addUsers")
    public ResponseEntity<String> addUsersToGroup(
            @RequestParam(value = "groupId") Integer groupId,
            @RequestParam(value = "userIds") ArrayList<Integer> userIds
    ) {
        logger.info("POST REQUEST /groups/addUsers");

        try {
            AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder()
                    .setGroupId(groupId)
                    .addAllUserIds(userIds)
                    .build();
            AddGroupMembersResponse response = groupsClientService.addGroupMembers(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/addUsers - an error occurred while adding a user to a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Post mapping for users to be removed from a group. Restricted to course administrators and teachers.
     *
     * @param userIds The users to be removed from the group.
     * @param groupId The group to which the use will be removed.
     * @return A response entity containing the status of the response and the response message.
     */
    @DeleteMapping("/groups/removeUsers")
    public ResponseEntity<String> removeUsersFromGroup(
            @RequestParam(value = "groupId") Integer groupId,
            @RequestParam(value = "userIds") ArrayList<Integer> userIds

    ) {
        logger.info("DELETE REQUEST /groups/removeUsers");

        try {
            RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder()
                    .setGroupId(groupId)
                    .addAllUserIds(userIds)
                    .build();
            RemoveGroupMembersResponse response = groupsClientService.removeGroupMembers(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/removeUsers - an error occurred while removing a user from a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
