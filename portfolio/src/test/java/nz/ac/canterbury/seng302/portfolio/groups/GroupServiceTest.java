package nz.ac.canterbury.seng302.portfolio.groups;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


class GroupServiceTest {

    private final GroupRepository repository = Mockito.mock(GroupRepository.class);

    private GroupService groupService;

    @BeforeEach
    public void setUp() {

        groupService = new GroupService(repository);
    }

    @Test
    void testAddUser() {

        Group group = new Group(1L, "Short", "Long");
        when(repository.findById(group.getId())).thenReturn(Optional.of(group));

        int userId = 1;
        groupService.addUserToGroup(group.getId(), userId);

        assertEquals(1, group.getMemberIds().size());
        assertEquals(userId, group.getMemberIds().get(0));

    }

    @Test
    void testAddAlreadyPresentUser() {

        Group group = new Group(1L, "Short", "Long");
        when(repository.findById(group.getId())).thenReturn(Optional.of(group));
        int userId = 1;

        groupService.addUserToGroup(group.getId(), userId);
        groupService.addUserToGroup(group.getId(), userId);

        assertEquals(1, group.getMemberIds().size());
        assertEquals(userId, group.getMemberIds().get(0));


    }

    @Test
    void testDeleteUser() {

        Group group = new Group(1L, "Short", "Long");
        when(repository.findById(group.getId())).thenReturn(Optional.of(group));
        int userId = 1;

        groupService.addUserToGroup(group.getId(), userId);
        groupService.removeUserFromGroup(group.getId(), userId);

        assertEquals(0, group.getMemberIds().size());

    }


}