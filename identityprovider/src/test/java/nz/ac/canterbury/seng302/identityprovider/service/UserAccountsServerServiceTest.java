package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserAccountsServerServiceTest {

    @Autowired
    UserRepository repository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    private UrlService urlService;

    @Autowired
    UserAccountsServerService service;

    User user;

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountStub;


    @BeforeEach
    void setUp() {
        user = new User(
                "MySuperCoolUsername",
                "password",
                "test",
                "test",
                "test",
                "test",
                "test",
                "test/test",
                "test@example.com",
                TimeService.getTimeStamp());
    }


    @Test
    void removeExistingRoleFromUser() {
        //Add some roles to the user
        user.addRole(UserRole.STUDENT);
        user.addRole(UserRole.TEACHER);

        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(user.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.removeRoleFromUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        User updatedUser = repository.findById(user.getId());

        Assertions.assertTrue(response.getIsSuccess());
        assertFalse(updatedUser.getRoles().contains(UserRole.TEACHER));
    }


    @Test
    void removeExistingRoleFromUserNoUserOfThatId() {
        //Add some roles to the user
        user.addRole(UserRole.STUDENT);
        user.addRole(UserRole.TEACHER);

        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(-1)
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.removeRoleFromUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Could not find user", response.getMessage());
        User updatedUser = repository.findById(user.getId());
        assertTrue(updatedUser.getRoles().contains(UserRole.TEACHER));
    }


    @Test
    void removeExistingRoleFromUserOnlyHasOneRole() {
        //Add some roles to the user
        user.addRole(UserRole.STUDENT);

        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.STUDENT)
                .setUserId(user.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.removeRoleFromUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("The user can't have zero roles", response.getMessage());
        User updatedUser = repository.findById(user.getId());
        assertTrue(updatedUser.getRoles().contains(UserRole.STUDENT));
    }


    @Test
    void registerNewUser() {
        groupRepository.deleteAll();
        repository.deleteAll();
        UserRegisterRequest.Builder request = UserRegisterRequest.newBuilder();
        request.setUsername(user.getUsername())
                .setPassword(user.getPwhash())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPronouns())
                .setNickname(user.getNickname());

        StreamObserver<UserRegisterResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRegisterResponse> responseCaptor = ArgumentCaptor.forClass(UserRegisterResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.register(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRegisterResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        assertTrue(repository.existsById(response.getNewUserId()));
    }


    @Test
    void registerNewUserUsernameInUse() {
        repository.save(user);

        UserRegisterRequest.Builder request = UserRegisterRequest.newBuilder();
        request.setUsername(user.getUsername())
                .setPassword(user.getPwhash())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPronouns())
                .setNickname(user.getNickname());

        StreamObserver<UserRegisterResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRegisterResponse> responseCaptor = ArgumentCaptor.forClass(UserRegisterResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.register(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRegisterResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertFalse(repository.existsById(response.getNewUserId()));
        assertEquals("Username already in use", response.getMessage());
    }


    @Test
    void getUserAccountById() {
        repository.save(user);

        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(user.getId());

        UserResponse.Builder expectedObject = UserResponse.newBuilder();

        expectedObject.setUsername(user.getUsername())
                .setId(user.getId())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setNickname(user.getNickname())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPronouns())
                .setEmail(user.getEmail())
                .setCreated(user.getAccountCreatedTime())
                .setProfileImagePath(urlService.getProfileURL(user).toString()
                );
        expectedObject.addRoles(UserRole.STUDENT);

        StreamObserver<UserResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserResponse> responseCaptor = ArgumentCaptor.forClass(UserResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.getUserAccountById(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserResponse response = responseCaptor.getValue();

        assertEquals(expectedObject.build(), response);
    }


    @Test
    void editUser() {
        groupRepository.deleteAll();
        repository.deleteAll();
        repository.save(user);

        EditUserRequest.Builder request = EditUserRequest.newBuilder();
        request.setUserId(user.getId())
                .setFirstName("Johnny")
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setNickname(user.getNickname())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPronouns())
                .setEmail(user.getEmail());

        StreamObserver<EditUserResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<EditUserResponse> responseCaptor = ArgumentCaptor.forClass(EditUserResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.editUser(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        EditUserResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        assertEquals("Johnny", repository.findById(user.getId()).getFirstName());
    }


    @Test
    void editUserNoUserOfThatId() {
        repository.save(user);

        EditUserRequest.Builder request = EditUserRequest.newBuilder();
        request.setUserId(-1)
                .setFirstName("Johnny")
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setNickname(user.getNickname())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPronouns())
                .setEmail(user.getEmail());

        StreamObserver<EditUserResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<EditUserResponse> responseCaptor = ArgumentCaptor.forClass(EditUserResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.editUser(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        EditUserResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Could not find user to edit", response.getMessage());
        assertEquals(user.getFirstName(), repository.findById(user.getId()).getFirstName());
    }


    @Test
    void changeUserPassword() {
        repository.save(user);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(user.getId())
                .setCurrentPassword("password")
                .setNewPassword("SuperSecurePassword");

        String salt = user.getSalt();
        LoginService encryptor = new LoginService();
        String expectedPassword = encryptor.getHash(request.getNewPassword(), salt);

        StreamObserver<ChangePasswordResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ChangePasswordResponse> responseCaptor = ArgumentCaptor.forClass(ChangePasswordResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.changeUserPassword(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ChangePasswordResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        assertEquals(expectedPassword, repository.findById(user.getId()).getPwhash());
    }


    @Test
    void changeUserPasswordIncorrectCurrentPassword() {
        repository.save(user);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(user.getId())
                .setCurrentPassword("is this my password?")
                .setNewPassword("SuperSecurePassword");

        String expectedPassword = user.getPwhash();

        StreamObserver<ChangePasswordResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ChangePasswordResponse> responseCaptor = ArgumentCaptor.forClass(ChangePasswordResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.changeUserPassword(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ChangePasswordResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Incorrect current password provided" , response.getMessage());
        assertEquals(expectedPassword, repository.findById(user.getId()).getPwhash());
    }


    @Test
    void changeUserPasswordNoUserOfThatId() {
        repository.save(user);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(-1)
                .setCurrentPassword("password")
                .setNewPassword("SuperSecurePassword");

        String expectedPassword = user.getPwhash();

        StreamObserver<ChangePasswordResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ChangePasswordResponse> responseCaptor = ArgumentCaptor.forClass(ChangePasswordResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.changeUserPassword(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ChangePasswordResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Could not find user" , response.getMessage());
        assertEquals(expectedPassword, repository.findById(user.getId()).getPwhash());
    }


    @Test
    void addRoleToUser() {
        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(user.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.addRoleToUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        User updatedUser = repository.findById(user.getId());
        assertTrue(updatedUser.getRoles().contains(UserRole.TEACHER));
    }


    @Test
    void addRoleToUserNoUserOfThatId() {
        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(-1)
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.addRoleToUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        User updatedUser = repository.findById(user.getId());
        assertFalse(updatedUser.getRoles().contains(UserRole.TEACHER));
        assertEquals("Could not find user", response.getMessage());
    }


    @Test
    void addRoleToUserAlreadyHasThatRole() {
        user.addRole(UserRole.TEACHER);
        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(user.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.addRoleToUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("User already has that role", response.getMessage());
    }


    @Test
    @Transactional
    void addTeacherRoleIsAddedToTeacherGroup() {
        Group teachingGroup = new Group(0, "Teachers", "Teaching Staff");
        groupRepository.save(teachingGroup);
        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(user.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.addRoleToUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        Optional<Group> group = groupRepository.findByShortName("Teachers");
        List<User> usersInTeachersGroup = null;
        if (group.isPresent()) {
            usersInTeachersGroup = group.get().getUserList();
        } else {
            fail("Teachers group not found");
        }
        assertTrue(usersInTeachersGroup.contains(user));
    }


    @Test
    @Transactional
    void removeTeacherRoleIsRemovedFromTeacherGroup() {

        User newUser = new User(
                "testuser",
                "password",
                "steve",
                "steve",
                "steve",
                "steve",
                "steve",
                "steve/steve",
                "steve@example.com",
                TimeService.getTimeStamp());
        //clear and repopulate repositories
        groupRepository.deleteAll();
        repository.deleteAll();
        newUser.addRole(UserRole.TEACHER);
        User newSavedUser = repository.save(newUser);
        Group teachingGroup = new Group( 1,"Teachers", "Teaching Staff");

        List<User> usersToAdd = new ArrayList<>();
        usersToAdd.add(newSavedUser);
        teachingGroup.addGroupMembers(usersToAdd);

        groupRepository.save(teachingGroup);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(newSavedUser.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.removeRoleFromUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        Optional<Group> group = groupRepository.findByShortName("Teachers");
        List<User> usersInTeachersGroup = null;
        if (group.isPresent()) {
            usersInTeachersGroup = group.get().getUserList();
        } else {
            fail("Teachers group not found");
        }
        assertFalse(usersInTeachersGroup.contains(newSavedUser));
    }
}