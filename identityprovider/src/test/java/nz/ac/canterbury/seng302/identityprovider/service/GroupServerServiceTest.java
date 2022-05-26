package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupService;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteGroupResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.GetGroupDetailsRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

class GroupServerServiceTest {


    private final GroupRepository groupRepository = Mockito.mock(GroupRepository.class);

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private final UserAccountsServerService userAccountsServerService = Mockito.spy(UserAccountsServerService.class);

    @InjectMocks
    private GroupsServerService groupService = new GroupsServerService();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

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

        StreamObserver<GroupDetailsResponse> responseObserver = new StreamObserver<GroupDetailsResponse>() {
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

        groupService.getGroupDetails(getGroupRequest, responseObserver);

    }



    @Test
    void testGetGroupDetailsNoGroupDoesNotExist() {

        when(groupRepository.existsById(Mockito.any())).thenReturn(false);

        GetGroupDetailsRequest getGroupRequest = GetGroupDetailsRequest.newBuilder().setGroupId(3).build();

        StreamObserver<GroupDetailsResponse> responseObserver = new StreamObserver<GroupDetailsResponse>() {
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

        groupService.getGroupDetails(getGroupRequest, responseObserver);

    }
}
