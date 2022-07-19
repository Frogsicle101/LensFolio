package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.TestData.TestGroupData;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.service.TimeService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;


class GroupTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository repository;

    @InjectMocks
    private TestGroupData testGroupData = Mockito.spy(TestGroupData.class);

    private final List<User> userList = new ArrayList<>();


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        User test1 = new User(
                "test1",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        User test2 = new User(
                "test2",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        User test3 = new User(
                "test3",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        User test4 = new User(
                "test4",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        User test5 = new User(
                "test5",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com",
                TimeService.getTimeStamp()
        );
        test1.addRole(UserRole.STUDENT);
        test2.addRole(UserRole.STUDENT);
        test3.addRole(UserRole.TEACHER);
        test4.addRole(UserRole.TEACHER);
        test5.addRole(UserRole.TEACHER);


        userList.add(test1);
        userList.add(test2);
        userList.add(test3);
        userList.add(test4);
        userList.add(test5);
    }


    @Test
    void TestAddDefaultGroups() {
        Mockito.when(repository.findAll()).thenReturn(userList);
        Mockito.when(groupRepository.getGroupById(0)).thenReturn(new Group(0, "Teachers", "Teaching Staff"));
        Mockito.when(groupRepository.getGroupById(1)).thenReturn(new Group(1, "Non-Group", "Members Without A Group"));
        ArgumentCaptor<Group> groupArgumentCaptor = ArgumentCaptor.forClass(Group.class);
        testGroupData.addDefaultGroups();
        testGroupData.setInitialTeachersAndMWAGGroupMembers();
        Mockito.verify(groupRepository, Mockito.atLeast(2)).save(groupArgumentCaptor.capture());
        List<Group> groups = groupArgumentCaptor.getAllValues();
        Group teachingGroup = groups.get(2);
        Group nonMemberGroup = groups.get(3);
        Assertions.assertEquals("Teachers", teachingGroup.getShortName());
        Assertions.assertEquals(3, teachingGroup.getMembersNumber());
        Assertions.assertEquals("Non-Group", nonMemberGroup.getShortName());
        Assertions.assertEquals(2, nonMemberGroup.getMembersNumber());
    }
}
