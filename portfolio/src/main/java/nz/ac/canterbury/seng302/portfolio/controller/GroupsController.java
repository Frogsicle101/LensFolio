package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.dto.GroupCreationDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.GroupResponseDTO;
import nz.ac.canterbury.seng302.portfolio.service.GroupService;
import nz.ac.canterbury.seng302.portfolio.service.UserService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
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

import java.util.List;
import java.util.Objects;

/**
 * The controller for managing requests to edit groups and their user's memberships.
 */
@Controller
public class GroupsController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final GroupsClientService groupsClientService;
    private final GroupService groupService;
    private final UserService userService;
    private final UserAccountsClientService userAccountsClientService;

    private int pageNum = 1;
    private int totalPages = 1;
    private int totalNumGroups = 0;
    private int groupsPerPageLimit = 10;
    private int offset = 0;
    private static final Integer TEACHER_GROUP_ID = 1;
    private static final String ORDER_BY = "shortName";
    private static final Boolean IS_ASCENDING = true;


    /**
     * Autowired constructor
     * @param groupsClientService The service class for GRPC stuff for Groups.
     * @param groupService The service class for related methods for Groups.
     * @param userService The service class for related methods for Users.
     * @param userAccountsClientService The user account service.
     */
    @Autowired
    GroupsController(GroupsClientService groupsClientService,
                     GroupService groupService,
                     UserService userService,
                     UserAccountsClientService userAccountsClientService){
        this.groupsClientService = groupsClientService;
        this.groupService = groupService;
        this.userService = userService;
        this.userAccountsClientService = userAccountsClientService;
    }


    /**
     * This endpoint retrieves all groups as a paginated list.
     *
     * @param principal The authentication principal.
     * @param page The page number to retrieve the groups.
     * @param groupsPerPage The number of groups you want to retrieve on that page.
     * @return The ModalAndView that contains the groups.
     */
    @GetMapping("/groups")
    public ModelAndView groups(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "groupsPerPage", required = false) String groupsPerPage)
    {
        logger.info("GET REQUEST /groups - attempt to get all groups");
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        ModelAndView modelAndView = new ModelAndView("groups");
        userService.checkAndAddUserRole(user, modelAndView);

        try {
            if (page != null) {
                pageNum = page;
            }
            if (pageNum <= 1) { //to ensure no negative page numbers
                pageNum = 1;
            }
            // check for new values
            if (groupsPerPage != null){
                switch(groupsPerPage){
                    case "20" -> this.groupsPerPageLimit = 20;
                    case "40" -> this.groupsPerPageLimit = 40;
                    case "60" -> this.groupsPerPageLimit = 60;
                    case "all" -> this.groupsPerPageLimit = 999999999;
                    default -> this.groupsPerPageLimit = 10;
                }
            }
            offset = (pageNum - 1) * groupsPerPageLimit; // The number to start retrieving groups from
            PaginatedGroupsResponse response = groupService.getPaginatedGroupsFromServer(offset, ORDER_BY, groupsPerPageLimit, IS_ASCENDING);
            totalNumGroups = response.getPaginationResponseOptions().getResultSetSize();
            totalPages = totalNumGroups / groupsPerPageLimit;
            if ((totalNumGroups % groupsPerPageLimit) != 0) {
                totalPages++; // Checks if there are leftover groups to display
            }
            if (pageNum > totalPages) { //to ensure that the last page will be shown if the page number is too large
                pageNum = totalPages;
                offset = (pageNum - 1) * groupsPerPageLimit;
                response = groupService.getPaginatedGroupsFromServer(offset, ORDER_BY, groupsPerPageLimit, IS_ASCENDING);
            }

            modelAndView.addObject("groups", response.getGroupsList());
            modelAndView.addObject("user", user);

        } catch (Exception e) {
            logger.error("ERROR /groups - an error occurred while retrieving groups");
            logger.error(e.getMessage());
            return new ModelAndView("errorPage").addObject(e.getMessage(), e);
        }

        return modelAndView;
    }


    /**
     * Gets an individual group by the group Id.
     *
     * @param groupId - The id group whose information is being retrieved
     * @return a Response entity containing the HTTPStatus and the groups information.
     */
    @GetMapping("/group")
    public ResponseEntity<Object> getGroup(@RequestParam Integer groupId) {
        logger.info("GET REQUEST /group - attempt to get group {}", groupId);
        try {
            GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                    .setGroupId(groupId)
                    .build();
            GroupDetailsResponse response = groupsClientService.getGroupDetails(request);
            return new ResponseEntity<>(new GroupResponseDTO(response), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("ERROR /group - an error occurred while retrieving group {}", groupId);
            logger.error(exception.getMessage());
            return new ResponseEntity<>("an error occurred while retrieving group", HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * The get request to get the create group html
     * @param principal - The user who made the request
     * @return ModelAndView - the model and view of the group creation page
     */
    @GetMapping("/groupsCreate")
    public ModelAndView getCreatePage(@AuthenticationPrincipal Authentication principal) {
        try {
            logger.info("GET REQUEST /groups/create - get group creation page");
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);

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
            return new ResponseEntity<>("an error occurred while deleting a group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Restricted to teachers and course administrators, This endpoint creates a new group.
     *
     * @param principal - The user who made the request
     * @param createInfo the group request that contains the short and long name
     * @return ResponseEntity - a response entity containing either CREATED or BAD_REQUEST (for now)
     */
    @PostMapping("/groups/edit")
    public ResponseEntity<String> createGroup(@AuthenticationPrincipal Authentication principal,
                                              @ModelAttribute(name="editDetailsForm") GroupCreationDTO createInfo) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("POST REQUEST /groups/edit - attempt to create group {} by user: {}", createInfo.getShortName(), userId);
        try {
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
            return new ResponseEntity<>("Unable to create group " + createInfo.getShortName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Restricted to teachers and course administrators, This endpoint modify a group details.
     *
     * @param principal The user who made the request.
     * @param groupId The id of the group to be modified.
     * @param shortName The new short name of the group.
     * @param longName  The new long name of the group.
     * @return A response entity containing either OK, BAD_REQUEST, or INTERNAL_SERVER_ERROR.
     */
    @PostMapping("/groups/edit/details")
    public ResponseEntity<String> modifyGroupDetails(@AuthenticationPrincipal Authentication principal,
                                                     @RequestParam Integer groupId,
                                                     @RequestParam String shortName,
                                                     @RequestParam String longName) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("POST REQUEST /groups/edit/details - attempt to modify details of group {} by user: {}", groupId, userId);
        return groupEdit(groupId, shortName, longName);
    }


    /**
     * Endpoint for students to edit their own groups longname.
     * Students have access for this endpoint, but can only modify the longname of a group they are in.
     *
     * @param principal The user who made the request.
     * @param groupId The id of the group to be modified.
     * @param longName  The new long name of the group.
     * @return A response entity containing either OK, BAD_REQUEST, or INTERNAL_SERVER_ERROR.
     */
    @PatchMapping("/groups/edit/longName")
    public ResponseEntity<String> modifyGroupLongName(@AuthenticationPrincipal Authentication principal,
                                                     @RequestParam Integer groupId,
                                                     @RequestParam String longName) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("PATCH REQUEST /groups/edit/longName - attempt to modify details of group {} by user: {}", groupId, userId);
        // Firstly, we have to find the shortname of the group
        GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        GroupDetailsResponse response = groupsClientService.getGroupDetails(request);

        // Checks if the user trying to edit is a member of the group being edited
        if (!response.getMembersList().stream().map(UserResponse::getId).toList().contains(userId)){
            return new ResponseEntity<>("Only members of this group can edit the name", HttpStatus.UNAUTHORIZED);
        }

        return groupEdit(groupId, response.getShortName(), longName);
    }


    /**
     * An extracted helper method that makes a request to the identity provider
     * to modify a group's details.
     *
     * @param groupId The id of the group to be modified
     * @param shortName The new short name of the group. Use "" to leave it unmodified.
     * @param longName The new long name of the group.
     * @return A response entity containing either OK, BAD_REQUEST, or INTERNAL_SERVER_ERROR.
     */
    private ResponseEntity<String> groupEdit(Integer groupId, String shortName, String longName) {
        try {
            ModifyGroupDetailsRequest request = ModifyGroupDetailsRequest.newBuilder()
                    .setGroupId(groupId)
                    .setShortName(shortName)
                    .setLongName(longName)
                    .build();
            ModifyGroupDetailsResponse response = groupsClientService.modifyGroupDetails(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/edit/ - an error occurred while modifying a group's details");
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred when editing group", HttpStatus.INTERNAL_SERVER_ERROR);
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
            @RequestParam Integer groupId,
            @RequestParam List<Integer> userIds
    ) {
        logger.info("POST REQUEST /groups/addUsers");

        try {
            AddGroupMembersResponse response = groupService.addUsersToGroup(groupId, userIds);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/addUsers - an error occurred while adding a user to a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>("an error occurred while adding a user to a group", HttpStatus.INTERNAL_SERVER_ERROR);
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
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "groupId") Integer groupId,
            @RequestParam(value = "userIds") List<Integer> userIds
    ) {
        logger.info("DELETE REQUEST /groups/removeUsers");

        try {
            if (Objects.equals(groupId, TEACHER_GROUP_ID)) {
                logger.info("Removing users from teacher group, checking user is admin");
                UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
                if (!userResponse.getRolesList().contains(UserRole.COURSE_ADMINISTRATOR)) {
                    return new ResponseEntity<>("You must be a course administrator to do this.", HttpStatus.UNAUTHORIZED);
                }

            }
            RemoveGroupMembersResponse response = groupService.removeUsersFromGroup(groupId, userIds);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/removeUsers - an error occurred while removing a user from a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>("an error occurred while removing a user from a group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
