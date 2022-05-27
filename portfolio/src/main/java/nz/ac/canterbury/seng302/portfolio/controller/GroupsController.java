package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.GroupRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.UserRequest;
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

import java.lang.annotation.Repeatable;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GroupsController {

    /**
     * For logging the requests related to groups
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * For user authentication
     */
    @Autowired
    private UserAccountsClientService userAccountsClientService;

    /**
     * For making gRpc requests to the IdP
     */
    @Autowired
    private GroupsClientService groupsClientService;


    /**
     * The get request to get the create group html
     * @param principal - The user who made the request
     * @return ModelAndView - the model and view of the group creation page
     */
    @GetMapping("/groups/create")
    public ModelAndView getCreatePage(@AuthenticationPrincipal AuthState principal) {
        try {
            logger.info("GET REQUEST /groups/create - get group creation page");
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

            ModelAndView model = new ModelAndView("groupCreation");
            model.addObject("user", user);
            return model;
        } catch (Exception err) {
            logger.error("GET /groups/create: {}", err.getMessage());
            return new ModelAndView("error");
        }
    }

    /**
     * Restricted to teachers and course administrators, This endpoint deletes an existing group.
     * <br>
     *
     * @param principal - The user who made the request
     * @param groupId   - The group Id of the group to be deleted
     * @return ResponseEntity - a response entity containing either OK or NOT FOUND (for now)
     */
    @DeleteMapping("/groups/edit")
    public ResponseEntity<String> deleteGroup(@AuthenticationPrincipal AuthState principal,
                                              @RequestParam Integer groupId) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
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
     * <br>
     *
     * @param principal - The user who made the request
     * @param createInfo the group request that contains the short and long name
     * @return ResponseEntity - a response entity containing either CREATED or BAD_REQUEST (for now)
     */
    @PostMapping("/groups/edit")
    public ResponseEntity<String> createGroup(@AuthenticationPrincipal AuthState principal,
                                              @ModelAttribute(name="editDetailsForm") GroupRequest createInfo) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        logger.info("POST REQUEST /groups/edit - attempt to create group {} by user: {}", createInfo.getShortName(), userId);
        try {
            System.out.println(createInfo.getShortName());
            System.out.println(createInfo.getLongName());
            CreateGroupRequest request = CreateGroupRequest.newBuilder()
                    .setShortName(createInfo.getShortName())
                    .setLongName(createInfo.getLongName())
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
     * Post mapping for a user to be added to a group. Restricted to course administrators and teachers.
     *
     * @param userIds  The users to be added to the group.
     * @param groupId The group to which the use will be added.
     * @return a response entity containing the status of the response and the response message
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
     * @param userIds  The users to be removed from the group.
     * @param groupId The group to which the use will be removed.
     * @return a response entity containing the status of the response and the response message
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
        } catch (
                Exception e) {
            logger.error("ERROR /groups/removeUsers - an error occurred while removing a user from a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
