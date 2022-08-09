package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked") // Suppresses intelliJ's warning for testing with mock StreamObservers
class GroupServerServiceTest {

    private final GroupRepository groupRepository = Mockito.mock(GroupRepository.class);

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    @InjectMocks
    private GroupsServerService groupsServerService = new GroupsServerService();

    @Mock
    private GroupService groupService = new GroupService(groupRepository, userRepository);

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

        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(1, response.getValidationErrorsCount());
        Assertions.assertEquals("Short name", response.getValidationErrors(0).getFieldName());
    }


    @Test
    void testCreateGroupLongNameInUse() {
        String shortName = "Valid";
        String longName = "Invalid";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);

        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(1, response.getValidationErrorsCount());
        Assertions.assertEquals("Long name", response.getValidationErrors(0).getFieldName());
    }


    @Test
    void testCreateGroupShortNameAndLongNameInUse() {
        String shortName = "Invalid";
        String longName = "Invalid";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);

        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(2, response.getValidationErrorsCount());
        Assertions.assertEquals("Short name", response.getValidationErrors(0).getFieldName());
        Assertions.assertEquals("Long name", response.getValidationErrors(1).getFieldName());
    }


    @Test
    void testCreateGroupEmptyShortName() {
        String shortName = "";
        String longName = "Valid";
        String expectedMessage = "Group short name has to be between 1 and 50 characters";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);
        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(1, response.getValidationErrorsCount());
        Assertions.assertEquals("Short name", response.getValidationErrors(0).getFieldName());
        Assertions.assertEquals(expectedMessage, response.getValidationErrors(0).getErrorText());
    }


    @Test
    void testCreateGroupOverMaximumLengthShortName() {
        String shortName = "This Is A Really Long Invalid Short Name Because It Is Over Fifty Characters";
        String longName = "Valid";
        String expectedMessage = "Group short name has to be between 1 and 50 characters";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);
        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(1, response.getValidationErrorsCount());
        Assertions.assertEquals("Short name", response.getValidationErrors(0).getFieldName());
        Assertions.assertEquals(expectedMessage, response.getValidationErrors(0).getErrorText());
    }


    @Test
    void testCreateGroupEmptyLongName() {
        String shortName = "Valid";
        String longName = "";
        String expectedMessage = "Group long name has to be between 1 and 100 characters";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);
        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(1, response.getValidationErrorsCount());
        Assertions.assertEquals("Long name", response.getValidationErrors(0).getFieldName());
        Assertions.assertEquals(expectedMessage, response.getValidationErrors(0).getErrorText());
    }

    @Test
    void testCreateGroupOverMaximumLengthLongName() {
        String shortName = "Valid";
        String longName = "This is an invalid group long name because it is over 100 characters which we don't allow. " +
                "this is 102";
        String expectedMessage = "Group long name has to be between 1 and 100 characters";

        CreateGroupResponse response = runCreateGroupTest(shortName, longName);
        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals(1, response.getValidationErrorsCount());
        Assertions.assertEquals("Long name", response.getValidationErrors(0).getFieldName());
        Assertions.assertEquals(expectedMessage, response.getValidationErrors(0).getErrorText());
    }

    // ------------------------------------------ Test deleteGroup -----------------------------------------------------

    @Test
    void testDeleteGroupSuccessful(){
        int groupId = 1; // Id 1 exists, Id 2 does not
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(groupId)
                .build();

        StreamObserver<DeleteGroupResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Mockito.when(groupRepository.existsById(1)).thenReturn(true);
        Mockito.when(groupRepository.getGroupById(Mockito.any())).thenReturn(new Group("Test", "Test"));
        Mockito.doNothing().when(groupRepository).deleteById(1);
        ArgumentCaptor<DeleteGroupResponse> responseCaptor = ArgumentCaptor.forClass(DeleteGroupResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.deleteGroup(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        DeleteGroupResponse response = responseCaptor.getValue();

        Assertions.assertTrue(response.getIsSuccess());
        Assertions.assertEquals("Successfully deleted the group with Id: 1", response.getMessage());
    }


    @Test
    void testDeleteGroupWithNonexistentGroup() {
        int groupId = 2; // Id 1 exists, Id 2 does not

        DeleteGroupResponse response = runDeleteGroupTest(groupId);

        Assertions.assertFalse(response.getIsSuccess());
        Assertions.assertEquals("No group exists with Id: 2", response.getMessage());
    }


    @Test
    void testGetGroupDetails() {
        Group group = new Group(1, "Short", "Long");
        ReflectionTestUtils.setField(group, "userList", new ArrayList<>());

        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        group.addGroupMember(user);
        group.addGroupMember(user2);

        StreamObserver<GroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<GroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(GroupDetailsResponse.class);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupRepository.existsById(Mockito.any())).thenReturn(true);
        when(groupRepository.getGroupById(Mockito.any())).thenReturn(group);
        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.findById(2)).thenReturn(user2);
        GetGroupDetailsRequest getGroupRequest = GetGroupDetailsRequest.newBuilder().setGroupId(1).build();


        groupsServerService.getGroupDetails(getGroupRequest, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        GroupDetailsResponse response = responseCaptor.getValue();

        List<UserResponse> userResponseList = response.getMembersList();
        Assertions.assertEquals(user.getUsername(), response.getMembers(0).getUsername());
        Assertions.assertEquals(user2.getUsername(), response.getMembers(1).getUsername());
        Assertions.assertEquals(2, userResponseList.size());
        Assertions.assertEquals(group.getLongName(), response.getLongName());
        Assertions.assertEquals(group.getShortName(), response.getShortName());
    }


    @Test
    void testGetGroupDetailsNoGroupDoesNotExist() {
        when(groupRepository.existsById(Mockito.any())).thenReturn(false);
        GetGroupDetailsRequest getGroupRequest = GetGroupDetailsRequest.newBuilder().setGroupId(3).build();

        StreamObserver<GroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<GroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(GroupDetailsResponse.class);

        groupsServerService.getGroupDetails(getGroupRequest, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        GroupDetailsResponse response = responseCaptor.getValue();

        Assertions.assertEquals(-1, response.getGroupId());

    }


    @Test
    void testGetTeachingGroup() {
        Group teachingGroup = new Group(0, "Teachers", "Teaching Staff");

        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);
        ReflectionTestUtils.setField(teachingGroup, "userList", userList);

        StreamObserver<GroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<GroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(GroupDetailsResponse.class);
        when(groupRepository.findByShortName("Teachers")).thenReturn(Optional.of(teachingGroup));
        when(groupRepository.existsById(Mockito.any())).thenReturn(true);
        when(groupRepository.getGroupById(Mockito.any())).thenReturn(teachingGroup);
        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.findById(2)).thenReturn(user2);
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.getTeachingStaffGroup(Empty.newBuilder().build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());

        GroupDetailsResponse response = responseCaptor.getValue();
        List<UserResponse> userResponseList = response.getMembersList();
        Assertions.assertEquals(user.getUsername(), response.getMembers(0).getUsername());
        Assertions.assertEquals(user2.getUsername(), response.getMembers(1).getUsername());
        Assertions.assertEquals(2, userResponseList.size());
        Assertions.assertEquals(teachingGroup.getLongName(), response.getLongName());
        Assertions.assertEquals(teachingGroup.getShortName(), response.getShortName());
    }


    @Test
    void testGetTeachingGroupWhenGroupDoesNotExist() {

        StreamObserver<GroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<GroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(GroupDetailsResponse.class);
        when(groupRepository.existsById(Mockito.any())).thenReturn(false);
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.getTeachingStaffGroup(Empty.newBuilder().build(), responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        GroupDetailsResponse response = responseCaptor.getValue();
        Assertions.assertEquals(-1, response.getGroupId());

    }


    @Test
    void testGetTeachingGroupThrowsException() {

        StreamObserver<GroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<GroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(GroupDetailsResponse.class);
        when(groupRepository.findByShortName("Teachers")).thenReturn(null);
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.getTeachingStaffGroup(Empty.newBuilder().build(), responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        GroupDetailsResponse response = responseCaptor.getValue();
        Assertions.assertEquals(-1, response.getGroupId());

    }


    @Test
    void testGetMembersWithoutAGroup() {
        Group nonGroup = new Group(1, "Non-Group", "Non-Group");

        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);
        ReflectionTestUtils.setField(nonGroup, "userList", userList);

        StreamObserver<GroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<GroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(GroupDetailsResponse.class);
        when(groupRepository.findByShortName("Non-Group")).thenReturn(Optional.of(nonGroup));
        when(groupRepository.existsById(Mockito.any())).thenReturn(true);
        when(groupRepository.getGroupById(Mockito.any())).thenReturn(nonGroup);
        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.findById(2)).thenReturn(user2);
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();


        groupsServerService.getMembersWithoutAGroup(Empty.newBuilder().build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());

        GroupDetailsResponse response = responseCaptor.getValue();
        List<UserResponse> userResponseList = response.getMembersList();
        Assertions.assertEquals(user.getUsername(), response.getMembers(0).getUsername());
        Assertions.assertEquals(user2.getUsername(), response.getMembers(1).getUsername());
        Assertions.assertEquals(2, userResponseList.size());
        Assertions.assertEquals(nonGroup.getLongName(), response.getLongName());
        Assertions.assertEquals(nonGroup.getShortName(), response.getShortName());
    }

    @Test
    void testGetMWAGWhenGroupDoesNotExist() {

        StreamObserver<GroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<GroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(GroupDetailsResponse.class);
        when(groupRepository.existsById(Mockito.any())).thenReturn(false);
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.getMembersWithoutAGroup(Empty.newBuilder().build(), responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        GroupDetailsResponse response = responseCaptor.getValue();
        Assertions.assertEquals(-1, response.getGroupId());

    }


    @Test
    void testGetMWAGThrowsException() {

        StreamObserver<GroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<GroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(GroupDetailsResponse.class);
        when(groupRepository.findByShortName("Non-Group")).thenReturn(null);
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.getMembersWithoutAGroup(Empty.newBuilder().build(), responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        GroupDetailsResponse response = responseCaptor.getValue();
        Assertions.assertEquals(-1, response.getGroupId());

    }



    @Test
    void testModifyGroupNoGroup() {
        ModifyGroupDetailsRequest modifyGroupDetailsRequest = ModifyGroupDetailsRequest.newBuilder().setGroupId(0).setLongName("test").setShortName("test").build();
        StreamObserver<ModifyGroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ModifyGroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(ModifyGroupDetailsResponse.class);
        when(groupRepository.findById(0)).thenReturn(Optional.empty());
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();
        groupsServerService.modifyGroupDetails(modifyGroupDetailsRequest, responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ModifyGroupDetailsResponse response = responseCaptor.getValue();
        Assertions.assertEquals("Could not find group to modify", response.getMessage());

    }

    @Test
    void testModifyGroup() {
        Group nonGroup = new Group(1, "Non-Group", "Non-Group");
        ModifyGroupDetailsRequest modifyGroupDetailsRequest = ModifyGroupDetailsRequest.newBuilder().setGroupId(1).setLongName("new long name").setShortName("Non-Group").build();
        StreamObserver<ModifyGroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ModifyGroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(ModifyGroupDetailsResponse.class);
        
        when(groupRepository.findById(1)).thenReturn(Optional.of(nonGroup));
        when(groupRepository.findByShortName("Non-Group")).thenReturn(Optional.of(nonGroup));
        when(groupRepository.findByLongName("Non-Group")).thenReturn(Optional.of(nonGroup));
        
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();
        groupsServerService.modifyGroupDetails(modifyGroupDetailsRequest, responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ModifyGroupDetailsResponse response = responseCaptor.getValue();
        Assertions.assertEquals("Successfully updated details for Non-Group", response.getMessage());
    }


    @Test
    void testModifyGroupSameShortName() {
        Group nonGroup = new Group(1, "Non-Group", "Non-Group");
        Group nonGroup2 = new Group(2, "Non-Group2", "Non-Group2");

        ModifyGroupDetailsRequest modifyGroupDetailsRequest = ModifyGroupDetailsRequest.newBuilder().setGroupId(1).setLongName("new long name").setShortName("Non-Group changes").build();
        StreamObserver<ModifyGroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ModifyGroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(ModifyGroupDetailsResponse.class);
        when(groupRepository.findById(1)).thenReturn(Optional.of(nonGroup));
        when(groupRepository.findByShortName("Non-Group changes")).thenReturn(Optional.of(nonGroup2));
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();
        groupsServerService.modifyGroupDetails(modifyGroupDetailsRequest, responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ModifyGroupDetailsResponse response = responseCaptor.getValue();
        Assertions.assertEquals("A group exists with the shortName Non-Group changes", response.getValidationErrors(0).getErrorText());
    }

    @Test
    void testModifyGroupSameLongName() {
        Group nonGroup = new Group(1, "Non-Group", "Non-Group");
        Group nonGroup2 = new Group(2, "Non-Group2", "Non-Group2");

        ModifyGroupDetailsRequest modifyGroupDetailsRequest = ModifyGroupDetailsRequest.newBuilder().setGroupId(1).setLongName("Non-Group").setShortName("Non-Group").build();
        StreamObserver<ModifyGroupDetailsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ModifyGroupDetailsResponse> responseCaptor = ArgumentCaptor.forClass(ModifyGroupDetailsResponse.class);
        when(groupRepository.findById(1)).thenReturn(Optional.of(nonGroup));
        when(groupRepository.findByLongName("Non-Group")).thenReturn(Optional.of(nonGroup2));
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();
        groupsServerService.modifyGroupDetails(modifyGroupDetailsRequest, responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ModifyGroupDetailsResponse response = responseCaptor.getValue();
        Assertions.assertEquals("A group already exists with the long name Non-Group", response.getValidationErrors(0).getErrorText());
    }



    @Test
    void removeGroupMembersTestException() {
        StreamObserver<RemoveGroupMembersResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<RemoveGroupMembersResponse> responseCaptor = ArgumentCaptor.forClass(RemoveGroupMembersResponse.class);
        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder().build();

        Mockito.doThrow(new RuntimeException("Broke")).when(groupService).removeGroupMembers(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.removeGroupMembers(request, responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        RemoveGroupMembersResponse response = responseCaptor.getValue();
        Assertions.assertFalse(response.getIsSuccess());
    }


    @Test
    void removeGroupMembersTest() {
        StreamObserver<RemoveGroupMembersResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<RemoveGroupMembersResponse> responseCaptor = ArgumentCaptor.forClass(RemoveGroupMembersResponse.class);
        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder().build();

        Mockito.doNothing().when(groupService).removeGroupMembers(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.removeGroupMembers(request, responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        RemoveGroupMembersResponse response = responseCaptor.getValue();
        Assertions.assertTrue(response.getIsSuccess());
    }


    @Test
    void addGroupMembersTestException() throws Exception {
        StreamObserver<AddGroupMembersResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<AddGroupMembersResponse> responseCaptor = ArgumentCaptor.forClass(AddGroupMembersResponse.class);
        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder().build();

        Mockito.doThrow(new RuntimeException("Broke")).when(groupService).addGroupMembers(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.addGroupMembers(request, responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        AddGroupMembersResponse response = responseCaptor.getValue();
        Assertions.assertFalse(response.getIsSuccess());
    }


    @Test
    void addGroupMembersTest() {
        StreamObserver<AddGroupMembersResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<AddGroupMembersResponse> responseCaptor = ArgumentCaptor.forClass(AddGroupMembersResponse.class);
        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder().build();

        Mockito.doNothing().when(groupService).removeGroupMembers(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.addGroupMembers(request, responseObserver);
        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        AddGroupMembersResponse response = responseCaptor.getValue();
        Assertions.assertTrue(response.getIsSuccess());
    }


    // ----------------------------------------- Test getPaginatedGroups ---------------------------------------------


    @Test
    void getPaginatedGroupsShortNameIncreasing(){
        String orderBy = "shortName";
        Integer offset = 0;
        Integer limit = 3;
        Boolean isAscending = true;

        PaginatedGroupsResponse response = runGetPaginatedGroupTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(3, response.getResultSetSize());
        Assertions.assertEquals(3, response.getGroupsCount());
        Assertions.assertEquals("Group 1", response.getGroupsList().get(0).getShortName());
        Assertions.assertEquals("Group 2", response.getGroupsList().get(1).getShortName());
        Assertions.assertEquals("Group 3", response.getGroupsList().get(2).getShortName());
    }


    @Test
    void getPaginatedGroupsShortNameDecreasing(){
        String orderBy = "shortName";
        Integer offset = 0;
        Integer limit = 3;
        Boolean isAscending = false;

        PaginatedGroupsResponse response = runGetPaginatedGroupTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(3, response.getResultSetSize());
        Assertions.assertEquals(3, response.getGroupsCount());
        Assertions.assertEquals("Group 3", response.getGroupsList().get(0).getShortName());
        Assertions.assertEquals("Group 2", response.getGroupsList().get(1).getShortName());
        Assertions.assertEquals("Group 1", response.getGroupsList().get(2).getShortName());
    }


    @Test
    void getPaginatedGroupsLongNameIncreasing(){
        String orderBy = "longName";
        Integer offset = 0;
        Integer limit = 3;
        Boolean isAscending = true;

        PaginatedGroupsResponse response = runGetPaginatedGroupTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(3, response.getResultSetSize());
        Assertions.assertEquals(3, response.getGroupsCount());
        Assertions.assertEquals("Group 1", response.getGroupsList().get(0).getShortName());
        Assertions.assertEquals("Group 2", response.getGroupsList().get(1).getShortName());
        Assertions.assertEquals("Group 3", response.getGroupsList().get(2).getShortName());
    }


    @Test
    void getPaginatedGroupsLongNameDecreasing(){
        String orderBy = "longName";
        Integer offset = 0;
        Integer limit = 3;
        Boolean isAscending = false;

        PaginatedGroupsResponse response = runGetPaginatedGroupTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(3, response.getResultSetSize());
        Assertions.assertEquals(3, response.getGroupsCount());
        Assertions.assertEquals("Group 3", response.getGroupsList().get(0).getShortName());
        Assertions.assertEquals("Group 2", response.getGroupsList().get(1).getShortName());
        Assertions.assertEquals("Group 1", response.getGroupsList().get(2).getShortName());
    }


    @Test
    void getPaginatedGroupsMemberCountIncreasing(){
        String orderBy = "membersNumber";
        Integer offset = 0;
        Integer limit = 3;
        Boolean isAscending = true;

        PaginatedGroupsResponse response = runGetPaginatedGroupTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(3, response.getResultSetSize());
        Assertions.assertEquals(3, response.getGroupsCount());
        Assertions.assertEquals("Group 1", response.getGroupsList().get(0).getShortName());
        Assertions.assertEquals("Group 3", response.getGroupsList().get(1).getShortName());
        Assertions.assertEquals("Group 2", response.getGroupsList().get(2).getShortName());
    }


    @Test
    void getPaginatedGroupsMemberCountDecreasing(){
        String orderBy = "membersNumber";
        Integer offset = 0;
        Integer limit = 3;
        Boolean isAscending = false;

        PaginatedGroupsResponse response = runGetPaginatedGroupTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(3, response.getResultSetSize());
        Assertions.assertEquals(3, response.getGroupsCount());
        Assertions.assertEquals("Group 2", response.getGroupsList().get(0).getShortName());
        Assertions.assertEquals("Group 3", response.getGroupsList().get(1).getShortName());
        Assertions.assertEquals("Group 1", response.getGroupsList().get(2).getShortName());
    }


    // ----------------------------------------- Test runner helpers -------------------------------------------------


    /**
     * A helper function for running tests for getting paginated groups
     *
     * @param orderBy The string of what parameter to order by
     * @param offset The amount of groups to offset the start of the list by
     * @param limit The maximum amount of groups to get for the page
     * @param isAscending Whether the list should be in ascending or descending order
     * @return The response received from the tested GroupsServerService.getPaginatedGroups method
     */
    private PaginatedGroupsResponse runGetPaginatedGroupTest(String orderBy, Integer offset, Integer limit, Boolean isAscending){
        GetPaginatedGroupsRequest request = GetPaginatedGroupsRequest.newBuilder()
                .setOffset(offset)
                .setLimit(limit)
                .setOrderBy(orderBy)
                .setIsAscendingOrder(isAscending)
                .build();

        List<Group> groupsList = createUsersAndAddToGroups();

        Mockito.when(groupRepository.findAll()).thenReturn(groupsList);
        StreamObserver<PaginatedGroupsResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<PaginatedGroupsResponse> responseCaptor = ArgumentCaptor.forClass(PaginatedGroupsResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.getPaginatedGroups(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        return responseCaptor.getValue();
    }


    /**
     * A helper function to set up some groups and users in these groups
     */
    private List<Group> createUsersAndAddToGroups() {
        List<Group> groupsList = new ArrayList<>();
        Group group1 = new Group(1,"Group 1", "Comp Sci Group 1");
        Group group2 = new Group(2,"Group 2", "Comp Sci Group 2");
        Group group3 = new Group(3,"Group 3", "Comp Sci Group 3");
        groupsList.add(group1);
        groupsList.add(group2);
        groupsList.add(group3);

        User user1 = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user3 = new User("Steve3", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());

        groupsList.get(1).addGroupMember(user1);
        groupsList.get(1).addGroupMember(user2);
        groupsList.get(2).addGroupMember(user3);

        return groupsList;
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

        StreamObserver<CreateGroupResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Mockito.when(groupRepository.findByShortName("Valid")).thenReturn(Optional.empty());
        Mockito.when(groupRepository.findByShortName("")).thenReturn(Optional.empty());
        Mockito.when(groupRepository.findByShortName("This Is A Really Long Invalid Short Name Because It Is " +
                "Over Fifty Characters")).thenReturn(Optional.empty());
        Mockito.when(groupRepository.findByShortName("Invalid")).thenReturn(Optional.of(testGroup));
        Mockito.when(groupRepository.findByLongName("Valid")).thenReturn(Optional.empty());
        Mockito.when(groupRepository.findByLongName("")).thenReturn(Optional.empty());
        Mockito.when(groupRepository.findByLongName("This is an invalid group long name because it is over 100 " +
                "characters which we don't allow. this is 102")).thenReturn(Optional.empty());
        Mockito.when(groupRepository.findByLongName("Invalid")).thenReturn(Optional.of(testGroup));
        Mockito.when(groupRepository.save(Mockito.any())).thenReturn(testGroup);
        ArgumentCaptor<CreateGroupResponse> responseCaptor = ArgumentCaptor.forClass(CreateGroupResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.createGroup(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        return responseCaptor.getValue();
    }


    /**
     * Helper function for running tests for deleting groups
     *
     * @param groupId - 1 is existing, 2 is non existing
     * @return The response received from the tested GroupsServerService.deleteGroup method
     */
    private DeleteGroupResponse runDeleteGroupTest(int groupId){
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                .setGroupId(groupId)
                .build();

        StreamObserver<DeleteGroupResponse> responseObserver = Mockito.mock(StreamObserver.class);
        Mockito.when(groupRepository.existsById(1)).thenReturn(true);
        Mockito.when(groupRepository.existsById(2)).thenReturn(false);
        Mockito.doNothing().when(groupRepository).deleteById(Mockito.any());
        ArgumentCaptor<DeleteGroupResponse> responseCaptor = ArgumentCaptor.forClass(DeleteGroupResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        groupsServerService.deleteGroup(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        return responseCaptor.getValue();
    }
}
