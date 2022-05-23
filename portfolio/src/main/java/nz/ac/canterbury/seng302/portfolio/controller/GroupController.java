//package nz.ac.canterbury.seng302.portfolio.controller;
//
//import nz.ac.canterbury.seng302.portfolio.groups.Group;
//import nz.ac.canterbury.seng302.portfolio.groups.GroupRepository;
//import nz.ac.canterbury.seng302.portfolio.groups.GroupService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//public class GroupController {
//
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    private final GroupService groupService;
//
//    public GroupController(GroupService groupService) {
//        this.groupService = groupService;
//    }
//
//
//    @PostMapping("/addUser")
//    public ResponseEntity<Object> addUserToGroup(
//            @RequestParam (value="groupId") Long groupId,
//            @RequestParam (value="userId") int userId
//    ) {
//        logger.info("POST REQUEST /portfolio/group/addUser");
//
//        try {
//            groupService.addUserToGroup(groupId, userId);
//            return new ResponseEntity<>(HttpStatus.OK);
//        } catch (IllegalArgumentException err) {
//            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception err){
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @DeleteMapping("/removeUser")
//    public ResponseEntity<Object> removeUserFromGroup(
//            @RequestParam (value="groupId") Long groupId,
//            @RequestParam (value="userId") int userId
//
//    ) {
//        logger.info("DELETE REQUEST /portfolio/group/removeUser");
//
//        try {
//            groupService.removeUserFromGroup(groupId, userId);
//            return new ResponseEntity<>(HttpStatus.OK);
//        } catch (IllegalArgumentException err) {
//            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception err){
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}
