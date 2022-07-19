package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.DTO.GroupDTO;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * For requesting user information form the IdP.
     */
    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private final int offset = 0;
    private final String orderBy = "shortname-increasing";
    private final int limit = 20;


    /**
     * This endpoint retrieves all groups as a paginated list.
     *
     * @return a response entity containing the list of GroupDetailsResponse object, and a response status.
     */
    @GetMapping("/groups")
    public ModelAndView groups(@AuthenticationPrincipal AuthState principal) {
        logger.info("GET REQUEST /groups - attempt to get all groups");

        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        ModelAndView modelAndView = new ModelAndView("groups");

        // Checks what role the user has. Adds boolean object to the view so that displays can be changed on the frontend.
        List<UserRole> roles = user.getRolesList();
        if (roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            modelAndView.addObject("userCanEdit", true);
        } else {
            modelAndView.addObject("userCanEdit", false);
        }

        //to populate groups page with groups
        try {
            GetPaginatedGroupsRequest request = GetPaginatedGroupsRequest.newBuilder()
                    .setOffset(offset)
                    .setOrderBy(orderBy)
                    .setLimit(limit)
                    .build();
            PaginatedGroupsResponse response = groupsClientService.getPaginatedGroups(request);

            modelAndView.addObject("groups", response.getGroupsList());
            modelAndView.addObject("user", user);

        } catch (Exception e) {
            logger.error("ERROR /groups - an error occurred while retrieving groups");
            logger.error(e.getMessage());
            return new ModelAndView("errorPage").addObject(e.getMessage(), e);
        }

        return modelAndView;
    }


    @GetMapping("/group")
    public ResponseEntity<Object> getGroup(@AuthenticationPrincipal AuthState principal,
                                   @RequestParam Integer groupId) {
        logger.info("GET REQUEST /group - attempt to get group {}", groupId);
        try {
            GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                    .setGroupId(groupId)
                    .build();
            GroupDetailsResponse response = groupsClientService.getGroupDetails(request);
            return new ResponseEntity<>(new GroupDTO(response), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("ERROR /groups - an error occurred while retrieving group {}", groupId);
            logger.error(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


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
