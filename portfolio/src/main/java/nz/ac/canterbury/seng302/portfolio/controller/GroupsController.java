package nz.ac.canterbury.seng302.portfolio.controller;

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

@Controller
public class GroupsController {

    /** For logging the requests related to groups */
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /** For making gRpc requests to the IdP */
    @Autowired
    private GroupsClientService groupsClientService;


    /**
     * Restricted to teachers and course administrators, This endpoint deletes an existing group.
     * <br>
     * @param principal - The user who made the request
     * @param groupId - The group Id of the group to be deleted
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
        }
        catch (Exception e) {
            logger.error("ERROR /groups/edit - an error occurred while deleting a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Restricted to teachers and course administrators, This endpoint creates a new group.
     * <br>
     * @param principal - The user who made the request
     * @param shortName - The short name of the group
     * @param longName -  The full name of the group
     * @return ResponseEntity - a response entity containing either CREATED or BAD_REQUEST (for now)
     */
    @PostMapping("/groups/edit")
    public ResponseEntity<String> createGroup(@AuthenticationPrincipal AuthState principal,
                                              @RequestParam String shortName,
                                              @RequestParam String longName) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
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
        }
        catch (Exception e) {
            logger.error("ERROR /groups/edit - an error occurred while creating a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
