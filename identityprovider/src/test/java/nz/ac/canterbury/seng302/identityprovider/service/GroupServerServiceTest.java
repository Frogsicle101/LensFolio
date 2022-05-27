package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

class GroupServerServiceTest {


    private final GroupRepository groupRepository = Mockito.mock(GroupRepository.class);

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private final UserAccountsServerService userAccountsServerService = Mockito.spy(UserAccountsServerService.class);

    @InjectMocks
    private GroupsServerService groupsServerService = new GroupsServerService();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------------------------------ Test createGroup -----------------------------------------------------

    @Test
    void testCreateGroupValidInformation() {
        String shortName = "Valid";
        String longName = "Valid";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);

        // Insert Assertions here
        Assertions.assertTrue(response.getIsSuccess());
        Assertions.assertEquals(1, response.getNewGroupId());
        Assertions.assertEquals("Created", response.getMessage());
        Assertions.assertEquals(0, response.getValidationErrorsCount());
    }


    @Test
    void testCreateGroupShortNameInUse() {
        String shortName = "Invalid";
        String longName = "Valid";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);

        // Insert Assertions here
        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(1, response.getValidationErrorsCount());
        Assertions.assertEquals("Short name", response.getValidationErrors(0).getFieldName());
    }


    @Test
    void testCreateGroupLongNameInUse() {
        String shortName = "Valid";
        String longName = "Invalid";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);

        // Insert Assertions here
        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(1, response.getValidationErrorsCount());
        Assertions.assertEquals("Long name", response.getValidationErrors(0).getFieldName());
    }


    @Test
    void testCreateGroupShortNameAndLongNameInUse() {
        String shortName = "Invalid";
        String longName = "Invalid";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);

        // Insert Assertions here
        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(2, response.getValidationErrorsCount());
        Assertions.assertEquals("Short name", response.getValidationErrors(0).getFieldName());
        Assertions.assertEquals("Long name", response.getValidationErrors(1).getFieldName());
    }

    // ------------------------------------------ Test deleteGroup -----------------------------------------------------

    @Test
    void testDeleteGroupSuccessful(){
        int groupId = 1; // Id 1 exists, Id 2 does not
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        // -----------------------------------------------

        StreamObserver<DeleteGroupResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Mockito.when(groupRepository.existsById(1)).thenReturn(true);
        Mockito.doNothing().when(groupRepository).deleteById(1);
        ArgumentCaptor<DeleteGroupResponse> responseCaptor = ArgumentCaptor.forClass(DeleteGroupResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        // Insert method call here
        // -----------------------------------------------
        groupsServerService.deleteGroup(request, responseObserver);

        // -----------------------------------------------
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        DeleteGroupResponse response = responseCaptor.getValue();

        // Insert Assertions here
        Assertions.assertTrue(response.getIsSuccess());
        Assertions.assertEquals("Successfully deleted the group with Id: 1", response.getMessage());
    }


    @Test
    void testDeleteGroupWithNonexistentGroup() {
        int groupId = 2; // Id 1 exists, Id 2 does not

        DeleteGroupResponse response = runDeleteGroupTest(groupId);

        // Insert Assertions here
        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals("No group exists with Id: 2", response.getMessage());
    }



    @Test
    void testGetGroupDetails() {
        Group group = new Group(1, "Short", "Long");
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        List<Integer> userInts = new ArrayList<>();
        userInts.add(1);
        userInts.add(2);
        group.addAllUsersToGroup(userInts);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupRepository.existsById(Mockito.any())).thenReturn(true);
        when(groupRepository.getGroupById(Mockito.any())).thenReturn(group);
        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.findById(2)).thenReturn(user2);


        GetGroupDetailsRequest getGroupRequest = GetGroupDetailsRequest.newBuilder().setGroupId(1).build();

        StreamObserver<GroupDetailsResponse> responseObserver = new StreamObserver<>() {
            List<UserResponse> userResponseList;


            @Override
            public void onNext(GroupDetailsResponse value) {
                userResponseList = value.getMembersList();
                Assertions.assertEquals(user.getUsername(), value.getMembers(0).getUsername());
                Assertions.assertEquals(user2.getUsername(), value.getMembers(1).getUsername());
                Assertions.assertEquals(2, userResponseList.size());
                Assertions.assertEquals(group.getLongName(), value.getLongName());
                Assertions.assertEquals(group.getShortName(), value.getShortName());

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        groupsServerService.getGroupDetails(getGroupRequest, responseObserver);

    }



    @Test
    void testGetGroupDetailsNoGroupDoesNotExist() {

        when(groupRepository.existsById(Mockito.any())).thenReturn(false);

        GetGroupDetailsRequest getGroupRequest = GetGroupDetailsRequest.newBuilder().setGroupId(3).build();

        StreamObserver<GroupDetailsResponse> responseObserver = new StreamObserver<>() {
            List<UserResponse> userResponseList;

            @Override
            public void onNext(GroupDetailsResponse value) {

                Assertions.assertEquals("NOT FOUND", value.getLongName());
                Assertions.assertEquals("", value.getShortName());

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };

        groupsServerService.getGroupDetails(getGroupRequest, responseObserver);

    }


    /**
     * Helper function for running tests for creating groups
     *
     * @param shortName - Use 'Valid' or 'Invalid' for not Already existing or existing names respectively
     * @param longName - Use 'Valid' or 'Invalid' for not Already existing or existing names respectively
     * @return The response received from the tested GroupsServerService.createGroup method
     */
    private CreateGroupResponse runCreateGroupTest(String shortName, String longName) {
        CreateGroupRequest request = CreateGroupRequest.newBuilder()
                .setShortName(shortName)
                .setLongName(longName)
                .build();
        Group testGroup = new Group(1, shortName, longName);
        // -----------------------------------------------

        StreamObserver<CreateGroupResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Mockito.when(groupRepository.findByShortName("Valid")).thenReturn(Optional.empty());
        Mockito.when(groupRepository.findByShortName("Invalid")).thenReturn(Optional.of(testGroup));
        Mockito.when(groupRepository.findByLongName("Valid")).thenReturn(Optional.empty());
        Mockito.when(groupRepository.findByLongName("Invalid")).thenReturn(Optional.of(testGroup));
        Mockito.when(groupRepository.save(Mockito.any())).thenReturn(testGroup);
        ArgumentCaptor<CreateGroupResponse> responseCaptor = ArgumentCaptor.forClass(CreateGroupResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        // Insert method call here
        // -----------------------------------------------
        groupsServerService.createGroup(request, responseObserver);

        // -----------------------------------------------
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        return responseCaptor.getValue();
    }


    /**
     * Helper function for running tests for deleting groups
     *
     * @param groupId - 1 is existing, 2 is non existing
     * @return The response received from the tested GroupsServerService.deleteGroup method
     */
    private DeleteGroupResponse runDeleteGroupTest(int groupId) {
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        // -----------------------------------------------

        StreamObserver<DeleteGroupResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Mockito.when(groupRepository.existsById(1)).thenReturn(true);
        Mockito.when(groupRepository.existsById(2)).thenReturn(false);
        Mockito.doNothing().when(groupRepository).deleteById(Mockito.any());
        ArgumentCaptor<DeleteGroupResponse> responseCaptor = ArgumentCaptor.forClass(DeleteGroupResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        // Insert method call here
        // -----------------------------------------------
        groupsServerService.deleteGroup(request, responseObserver);

        // -----------------------------------------------
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        return responseCaptor.getValue();
    }
}
