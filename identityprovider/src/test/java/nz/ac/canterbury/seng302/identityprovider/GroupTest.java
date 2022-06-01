package nz.ac.canterbury.seng302.identityprovider;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.service.TimeService;
import nz.ac.canterbury.seng302.identityprovider.service.UrlService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.Mockito.*;


class GroupTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository repository;



    @InjectMocks
    private IdentityProviderApplication identityProviderApplication = Mockito.spy(IdentityProviderApplication.class);


    private List<User> userList = new ArrayList<>();


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
        ArgumentCaptor<Group> groupArgumentCaptor = ArgumentCaptor.forClass(Group.class);
        identityProviderApplication.addDefaultGroups();
        Mockito.verify(groupRepository, Mockito.atLeast(2)).save(groupArgumentCaptor.capture());
        List<Group> groups = groupArgumentCaptor.getAllValues();
        Group teachingGroup = groups.get(0);
        Group nonMemberGroup = groups.get(1);
        Assertions.assertEquals("Teachers", teachingGroup.getShortName());
        Assertions.assertEquals(3, teachingGroup.getMembersNumber());
        Assertions.assertEquals("Non-Group", nonMemberGroup.getShortName());
        Assertions.assertEquals(2, nonMemberGroup.getMembersNumber());


    }


}
