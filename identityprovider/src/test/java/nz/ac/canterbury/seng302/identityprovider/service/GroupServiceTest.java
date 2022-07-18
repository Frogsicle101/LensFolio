package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


class GroupServiceTest {

    private final GroupRepository groupRepository = Mockito.mock(GroupRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private GroupService groupService;

    @BeforeEach
    public void setUp() {
        groupService = new GroupService(groupRepository, userRepository);
    }

    @Test
    void testAddUser() {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);


        Group group = new Group(1, "Short", "Long");
        ReflectionTestUtils.setField(group, "userList", new ArrayList<>());
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);

        assertEquals(2, group.getUserList().size());
        assertEquals(userList, group.getUserList());
    }

    @Test
    void testAddAlreadyPresentUser() {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);

        Group group = new Group(1, "Short", "Long");
        ReflectionTestUtils.setField(group, "userList", new ArrayList<>());
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);
        groupService.addGroupMembers(group.getId(), userIds);

        assertEquals(2, group.getMembersNumber());
        assertEquals(userList, group.getUserList());
    }

    @Test
    void testDeleteUser() {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);


        Group group = new Group(1, "Short", "Long");
        ReflectionTestUtils.setField(group, "userList", new ArrayList<>());
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);
        groupService.removeGroupMembers(group.getId(), userIds);

        assertEquals(0, group.getMembersNumber());

    }


}