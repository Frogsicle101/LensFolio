package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeAll;

public class GroupControllerTest {

//    private final GroupRepository mockGroupRepository = mock(GroupRepository.class);
//    private final GroupService groupService = new GroupService(mockGroupRepository);
//    private final GroupController groupController = new GroupController(groupService);
//    private final Group group = new Group(1L, "shortName", "longName");


    @BeforeAll
    public static void setup() {
        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("bio")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.TEACHER);
        UserResponse user = userBuilder.build();

        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();


    }
//
//    @Test
//    void addUserToGroup() {
//        Mockito.when(mockGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
//        ResponseEntity<Object> response = groupController.addUserToGroup(1L, 1);
//        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    void addUserToGroupNotAGroup() {
//        Mockito.when(mockGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
//        ResponseEntity<Object> response = groupController.addUserToGroup(2L, 1);
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//    }
//
//    @Test
//    void addUserToGroupNotUser() {
//        Mockito.when(mockGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
//        ResponseEntity<Object> response = groupController.addUserToGroup(1L, 2);
//        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//    }
//
//    @Test
//    void deleteUser() {
//        Mockito.when(mockGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
//        Mockito.when()
//        ResponseEntity<Object> response = groupController.removeUserFromGroup(1L, 1);
//        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    void deleteUserNotAGroup() {
//        Mockito.when(mockGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
//        ResponseEntity<Object> response = groupController.removeUserFromGroup(2L, 1);
//        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    void deleteUserNotInGroup() {
//        Mockito.when(mockGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
//        ResponseEntity<Object> response = groupController.removeUserFromGroup(1L, 1);
//        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
}
