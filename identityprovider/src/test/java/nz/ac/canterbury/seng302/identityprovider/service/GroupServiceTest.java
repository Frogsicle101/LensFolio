package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
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

        Group group = new Group(1, "Short", "Long");
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);

        assertEquals(2, group.getMemberIds().size());
        assertEquals(userIds, group.getMemberIds());

    }

    @Test
    void testAddAlreadyPresentUser() {

        Group group = new Group(1, "Short", "Long");
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);
        groupService.addGroupMembers(group.getId(), userIds);

        assertEquals(2, group.getMemberIds().size());
        assertEquals(userIds, group.getMemberIds());
    }

    @Test
    void testDeleteUser() {

        Group group = new Group(1, "Short", "Long");
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);
        groupService.removeGroupMembers(group.getId(), userIds);

        assertEquals(0, group.getMemberIds().size());

    }


}