package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked") // Suppresses intelliJ's warning for testing with mock StreamObservers
@RunWith(MockitoJUnitRunner.class)
class UserAccountsServerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Environment env;

    @Mock
    private GroupRepository groupRepository;

    private UrlService urlService;

    private UserAccountsServerService userAccountsServerService;

    private User initialUser;

    private UserResponse initialUserResponse;

    private User postOperationUser;

    private Group mwagGroup;

    private Group teacherGroup;

    private Group postOperationGroup;


    @BeforeEach
    void setUp() throws PasswordEncryptionException {
        initialUser = new User(
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

        teacherGroup = new Group(1, "Teachers", "Teaching staff group");
        mwagGroup = new Group(2, "Non-Group", "Members Without A Group");

        initialiseMocks();
        urlService = new UrlService(env);
        GroupService groupService = new GroupService(groupRepository, userRepository);
        userAccountsServerService = new UserAccountsServerService(userRepository, env, groupService);

        Mockito.when(groupRepository.findByShortName(mwagGroup.getShortName())).thenReturn(Optional.of(mwagGroup));
        Mockito.when(groupRepository.findByShortName(teacherGroup.getShortName())).thenReturn(Optional.of(teacherGroup));
        Mockito.when(groupRepository.findById(mwagGroup.getId())).thenReturn(Optional.of(mwagGroup));
        Mockito.when(groupRepository.findById(teacherGroup.getId())).thenReturn(Optional.of(teacherGroup));

        initialUserResponse = UserResponse.newBuilder()
                .setUsername(initialUser.getUsername())
                .setFirstName(initialUser.getFirstName())
                .setMiddleName(initialUser.getMiddleName())
                .setLastName(initialUser.getLastName())
                .setNickname(initialUser.getNickname())
                .setBio(initialUser.getBio())
                .setPersonalPronouns(initialUser.getPronouns())
                .setEmail(initialUser.getEmail())
                .setCreated(initialUser.getAccountCreatedTime())
                .setId(initialUser.getId())
                .setProfileImagePath(urlService.getProfileURL(initialUser).toString())
                .addAllRoles(initialUser.getRoles())
                .build();

        Mockito.when(initialUser.userResponse()).thenReturn(initialUserResponse);
    }


    private void initialiseMocks() {
        String defaultPhotoLocation = "src/main/resources/profile-photos/";
        Mockito.when(env.getProperty("photoLocation", defaultPhotoLocation)).thenReturn(defaultPhotoLocation);

        Mockito.when(env.getProperty("protocol", "http")).thenReturn("http");
        Mockito.when(env.getProperty("hostName", "localhost")).thenReturn(defaultPhotoLocation);
        Mockito.when(env.getProperty("port", "9001")).thenReturn("9001");
        Mockito.when(env.getProperty("rootPath", "")).thenReturn("");


        Mockito.when(userRepository.findById(initialUser.getId())).thenReturn(initialUser);
        Mockito.when(userRepository.findByUsername(initialUser.getUsername())).thenReturn(initialUser);
        Mockito.when(userRepository.findById(-1)).thenReturn(null);
    }


    @Test
    void removeExistingRoleFromUser() {
        //Add some roles to the user
        initialUser.addRole(UserRole.STUDENT);
        initialUser.addRole(UserRole.TEACHER);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(initialUser.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.removeRoleFromUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        Mockito.verify(userRepository).save(userCaptor.capture());
        postOperationUser = userCaptor.getValue();

        Assertions.assertTrue(response.getIsSuccess());
        assertFalse(postOperationUser.getRoles().contains(UserRole.TEACHER));
        assertTrue(postOperationUser.getRoles().contains(UserRole.STUDENT));
    }


    @Test
    void removeExistingRoleFromUserNoUserOfThatId() {
        //Add some roles to the user
        initialUser.addRole(UserRole.STUDENT);
        initialUser.addRole(UserRole.TEACHER);

        Mockito.when(userRepository.findById(initialUser.getId())).thenReturn(initialUser);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(-1)
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.removeRoleFromUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Could not find user", response.getMessage());
        User updatedUser = userRepository.findById(initialUser.getId());
        assertTrue(updatedUser.getRoles().contains(UserRole.TEACHER));
    }


    @Test
    void removeExistingRoleFromUserOnlyHasOneRole() {
        //Add some roles to the user
        initialUser.addRole(UserRole.STUDENT);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.STUDENT)
                .setUserId(initialUser.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.removeRoleFromUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());

        assertFalse(response.getIsSuccess());
        assertEquals("The user can't have zero roles", response.getMessage());
    }


    @Test
    void registerNewUser() {
        UserRegisterRequest.Builder request = UserRegisterRequest.newBuilder();
        request.setUsername(initialUser.getUsername())
                .setPassword(initialUser.getPwhash())
                .setFirstName(initialUser.getFirstName())
                .setMiddleName(initialUser.getMiddleName())
                .setLastName(initialUser.getLastName())
                .setEmail(initialUser.getEmail())
                .setBio(initialUser.getBio())
                .setPersonalPronouns(initialUser.getPronouns())
                .setNickname(initialUser.getNickname());

        Mockito.when(userRepository.findByUsername(initialUser.getUsername())).thenReturn(null);

        StreamObserver<UserRegisterResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRegisterResponse> responseCaptor = ArgumentCaptor.forClass(UserRegisterResponse.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.register(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRegisterResponse response = responseCaptor.getValue();

        Mockito.verify(userRepository).save(userCaptor.capture());
        postOperationUser = userCaptor.getValue();

        assertTrue(response.getIsSuccess());
        assertEquals(request.getUsername(), postOperationUser.getUsername());
    }


    @Test
    void registerNewUserUsernameInUse() {
        UserRegisterRequest.Builder request = UserRegisterRequest.newBuilder();
        request.setUsername(initialUser.getUsername())
                .setPassword(initialUser.getPwhash())
                .setFirstName(initialUser.getFirstName())
                .setMiddleName(initialUser.getMiddleName())
                .setLastName(initialUser.getLastName())
                .setEmail(initialUser.getEmail())
                .setBio(initialUser.getBio())
                .setPersonalPronouns(initialUser.getPronouns())
                .setNickname(initialUser.getNickname());

        StreamObserver<UserRegisterResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRegisterResponse> responseCaptor = ArgumentCaptor.forClass(UserRegisterResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.register(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRegisterResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Username already in use", response.getMessage());
    }


    @Test
    void getUserAccountById() {

        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(initialUser.getId());

        UserResponse.Builder expectedObject = UserResponse.newBuilder();

        expectedObject.setUsername(initialUser.getUsername())
                .setId(initialUser.getId())
                .setFirstName(initialUser.getFirstName())
                .setMiddleName(initialUser.getMiddleName())
                .setLastName(initialUser.getLastName())
                .setNickname(initialUser.getNickname())
                .setBio(initialUser.getBio())
                .setPersonalPronouns(initialUser.getPronouns())
                .setEmail(initialUser.getEmail())
                .setCreated(initialUser.getAccountCreatedTime())
                .setProfileImagePath(urlService.getProfileURL(initialUser).toString()
                );
        expectedObject.addRoles(UserRole.STUDENT);

        StreamObserver<UserResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserResponse> responseCaptor = ArgumentCaptor.forClass(UserResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.getUserAccountById(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserResponse response = responseCaptor.getValue();

        assertEquals(expectedObject.build(), response);
    }


    @Test
    void editUser() {
        groupRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(initialUser);

        EditUserRequest.Builder request = EditUserRequest.newBuilder();
        request.setUserId(initialUser.getId())
                .setFirstName("Johnny")
                .setMiddleName(initialUser.getMiddleName())
                .setLastName(initialUser.getLastName())
                .setNickname(initialUser.getNickname())
                .setBio(initialUser.getBio())
                .setPersonalPronouns(initialUser.getPronouns())
                .setEmail(initialUser.getEmail());

        StreamObserver<EditUserResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<EditUserResponse> responseCaptor = ArgumentCaptor.forClass(EditUserResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.editUser(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        EditUserResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        assertEquals("Johnny", userRepository.findById(initialUser.getId()).getFirstName());
    }


    @Test
    void editUserNoUserOfThatId() {
        userRepository.save(initialUser);

        EditUserRequest.Builder request = EditUserRequest.newBuilder();
        request.setUserId(-1)
                .setFirstName("Johnny")
                .setMiddleName(initialUser.getMiddleName())
                .setLastName(initialUser.getLastName())
                .setNickname(initialUser.getNickname())
                .setBio(initialUser.getBio())
                .setPersonalPronouns(initialUser.getPronouns())
                .setEmail(initialUser.getEmail());

        StreamObserver<EditUserResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<EditUserResponse> responseCaptor = ArgumentCaptor.forClass(EditUserResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.editUser(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        EditUserResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Could not find user to edit", response.getMessage());
        assertEquals(initialUser.getFirstName(), userRepository.findById(initialUser.getId()).getFirstName());
    }


    @Test
    void changeUserPassword() throws PasswordEncryptionException {
        userRepository.save(initialUser);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(initialUser.getId())
                .setCurrentPassword("password")
                .setNewPassword("SuperSecurePassword");

        String salt = initialUser.getSalt();
        LoginService encryptor = new LoginService();
        String expectedPassword = encryptor.getHash(request.getNewPassword(), salt);

        StreamObserver<ChangePasswordResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ChangePasswordResponse> responseCaptor = ArgumentCaptor.forClass(ChangePasswordResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.changeUserPassword(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ChangePasswordResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        assertEquals(expectedPassword, userRepository.findById(initialUser.getId()).getPwhash());
    }


    @Test
    void changeUserPasswordIncorrectCurrentPassword() {
        userRepository.save(initialUser);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(initialUser.getId())
                .setCurrentPassword("is this my password?")
                .setNewPassword("SuperSecurePassword");

        String expectedPassword = initialUser.getPwhash();

        StreamObserver<ChangePasswordResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ChangePasswordResponse> responseCaptor = ArgumentCaptor.forClass(ChangePasswordResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.changeUserPassword(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ChangePasswordResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Incorrect current password provided" , response.getMessage());
        assertEquals(expectedPassword, userRepository.findById(initialUser.getId()).getPwhash());
    }


    @Test
    void changeUserPasswordNoUserOfThatId() {
        userRepository.save(initialUser);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(-1)
                .setCurrentPassword("password")
                .setNewPassword("SuperSecurePassword");

        String expectedPassword = initialUser.getPwhash();

        StreamObserver<ChangePasswordResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<ChangePasswordResponse> responseCaptor = ArgumentCaptor.forClass(ChangePasswordResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.changeUserPassword(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        ChangePasswordResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("Could not find user" , response.getMessage());
        assertEquals(expectedPassword, userRepository.findById(initialUser.getId()).getPwhash());
    }


    @Test
    void addRoleToUser() {
        userRepository.save(initialUser);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(initialUser.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.addRoleToUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        User updatedUser = userRepository.findById(initialUser.getId());
        assertTrue(updatedUser.getRoles().contains(UserRole.TEACHER));
    }


    @Test
    void addRoleToUserNoUserOfThatId() {
        userRepository.save(initialUser);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(-1)
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.addRoleToUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        User updatedUser = userRepository.findById(initialUser.getId());
        assertFalse(updatedUser.getRoles().contains(UserRole.TEACHER));
        assertEquals("Could not find user", response.getMessage());
    }


    @Test
    void addRoleToUserAlreadyHasThatRole() {
        initialUser.addRole(UserRole.TEACHER);
        userRepository.save(initialUser);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(initialUser.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.addRoleToUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        assertFalse(response.getIsSuccess());
        assertEquals("User already has that role", response.getMessage());
    }


    @Test
    @Transactional
    void addTeacherRoleIsAddedToTeacherGroup() throws PasswordEncryptionException {
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
        userRepository.deleteAll();
        User newSavedUser = userRepository.save(newUser);

        Group teachingGroup = new Group( 1,"Teachers", "Teaching Staff");
        groupRepository.save(teachingGroup);
        Group MwagGroup = new Group(2, "Non-Group", "Members without a group");
        groupRepository.save(MwagGroup);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(newSavedUser.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.addRoleToUser(request, responseObserver);

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
        assertTrue(usersInTeachersGroup.contains(newSavedUser));
    }


    @Test
    @Transactional
    void removeTeacherRoleIsRemovedFromTeacherGroup() throws PasswordEncryptionException {

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
        userRepository.deleteAll();
        newUser.addRole(UserRole.TEACHER);
        User newSavedUser = userRepository.save(newUser);

        Group teachingGroup = new Group( 1,"Teachers", "Teaching Staff");
        teachingGroup.addGroupMember(newSavedUser);
        groupRepository.save(teachingGroup);

        Group MwagGroup = new Group(2, "Non-Group", "Members without a group");
        groupRepository.save(MwagGroup);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(newSavedUser.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.removeRoleFromUser(request, responseObserver);

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


    @Test
    @Transactional
    void registerNewUserAddedToMwag() {
        UserRegisterRequest.Builder request = UserRegisterRequest.newBuilder();
        request.setUsername(initialUser.getUsername())
                .setPassword(initialUser.getPwhash())
                .setFirstName(initialUser.getFirstName())
                .setMiddleName(initialUser.getMiddleName())
                .setLastName(initialUser.getLastName())
                .setEmail(initialUser.getEmail())
                .setBio(initialUser.getBio())
                .setPersonalPronouns(initialUser.getPronouns())
                .setNickname(initialUser.getNickname());

        StreamObserver<UserRegisterResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRegisterResponse> responseCaptor = ArgumentCaptor.forClass(UserRegisterResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        userAccountsServerService.register(request.build(), responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRegisterResponse response = responseCaptor.getValue();

        assertTrue(response.getIsSuccess());
        Optional<Group> group = groupRepository.findByShortName("Non-Group");
        List<User> usersInMwagGroup = null;
        if (group.isPresent()) {
            usersInMwagGroup = group.get().getUserList();
        } else {
            fail("Members Without A Group not found");
        }
        assertTrue(usersInMwagGroup.contains(initialUser));
    }


    @Test
    @Transactional
    void getPaginatedUsersFirstNameIncreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "firstname";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveA", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersFirstNameDecreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "firstname";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = false;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveF", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveA", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersMiddleNameIncreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "middlename";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveB", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveA", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersMiddleNameDecreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "middlename";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = false;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveE", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveA", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersLastNameIncreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "lastname";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveF", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveA", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersLastNameDecreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "lastname";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = false;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveA", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersUserNameIncreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "username";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveA", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersUserNameDecreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "username";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = false;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveF", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveA", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersNicknameIncreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "aliases";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveD", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveA", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersNicknameDecreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "aliases";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = false;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveC", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveA", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersRolesIncreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "roles";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveA", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersRolesDecreasing() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "roles";
        Integer offset = 0;
        Integer limit = 6;
        Boolean isAscending = false;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals("SteveD", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveA", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersOffsetThree() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "firstname";
        Integer offset = 3;
        Integer limit = 6;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals(3, response.getUsersList().size());
        Assertions.assertEquals("SteveD", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(2).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersOffsetMoreThanAmountUsers() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "firstname";
        Integer offset = 7;
        Integer limit = 6;
        Boolean isAscending = false;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals(0, response.getUsersList().size());
    }


    @Test
    @Transactional
    void getPaginatedUsersLimitThree() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "firstname";
        Integer offset = 0;
        Integer limit = 3;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals(3, response.getUsersList().size());
        Assertions.assertEquals("SteveA", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(2).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersLimitHigherThanAmountUsers() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "firstname";
        Integer offset = 0;
        Integer limit = 7;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals(6, response.getUsersList().size());
        Assertions.assertEquals("SteveA", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveB", response.getUsers(1).getUsername());
        Assertions.assertEquals("SteveC", response.getUsers(2).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(3).getUsername());
        Assertions.assertEquals("SteveE", response.getUsers(4).getUsername());
        Assertions.assertEquals("SteveF", response.getUsers(5).getUsername());
    }


    @Test
    @Transactional
    void getPaginatedUsersOffsetTwoLimitTwo() throws PasswordEncryptionException{
        groupRepository.deleteAll();
        userRepository.deleteAll();
        String orderBy = "firstname";
        Integer offset = 2;
        Integer limit = 2;
        Boolean isAscending = true;

        PaginatedUsersResponse response = runGetPaginatedUsersTest(orderBy, offset, limit, isAscending);
        Assertions.assertEquals(6, response.getPaginationResponseOptions().getResultSetSize());
        Assertions.assertEquals(2, response.getUsersList().size());
        Assertions.assertEquals("SteveC", response.getUsers(0).getUsername());
        Assertions.assertEquals("SteveD", response.getUsers(1).getUsername());
    }

    // ----------------------------------------- Test runner helpers -------------------------------------------------


    /**
     * A helper function for running tests for getting paginated users
     *
     * @param orderBy The string of what parameter to order by
     * @param offset The amount of users to offset the start of the list by
     * @param limit The maximum amount of users to get for the page
     * @param isAscending Whether the list should be in ascending or descending order
     * @return The response received from the tested UserAccountsServerService.getPaginatedUsers method
     */
    private PaginatedUsersResponse runGetPaginatedUsersTest(String orderBy, Integer offset, Integer limit, Boolean isAscending) throws PasswordEncryptionException{
        PaginationRequestOptions options = PaginationRequestOptions.newBuilder()
                .setOffset(offset)
                .setLimit(limit)
                .setOrderBy(orderBy)
                .setIsAscendingOrder(isAscending)
                .build();
        GetPaginatedUsersRequest request = GetPaginatedUsersRequest.newBuilder()
                .setPaginationRequestOptions(options)
                .build();

        StreamObserver<PaginatedUsersResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<PaginatedUsersResponse> responseCaptor = ArgumentCaptor.forClass(PaginatedUsersResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        List<User> usersList = createUsers();
        userRepository.saveAll(usersList);

        userAccountsServerService.getPaginatedUsers(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        return responseCaptor.getValue();
    }


    /**
     * A helper function to create the users used for testing. The fields are a mix of upper and lower case letters and
     * are in different orders to ensure that the sorting is working as expected
     *
     * @return A list of users to be saved to the repository
     */
    private List<User> createUsers() throws PasswordEncryptionException {
        List<User> userList = new ArrayList<>();
        User user1 = new User("SteveA", "password", "Stevea", "Stevensonb", "McSteveF", "KingSteved", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user2 = new User("SteveB", "password", "SteveB", "StevensonA", "McSteveE", "KingStevee", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user3 = new User("SteveC", "password", "Stevec", "Stevensond", "McSteveD", "KingStevef", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user4 = new User("SteveD", "password", "SteveD", "StevensonC", "McStevec", "KingSteveA", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user5 = new User("SteveE", "password", "Stevee", "Stevensonf", "McSteveb", "KingSteveB", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        User user6 = new User("SteveF", "password", "SteveF", "StevensonE", "McStevea", "KingSteveC", "", "", "Steve@steve.com", Timestamp.newBuilder().build());
        user1.addRole(UserRole.STUDENT);
        user2.addRole(UserRole.TEACHER);
        user3.addRole(UserRole.COURSE_ADMINISTRATOR);
        user4.addRole(UserRole.TEACHER);
        user4.addRole(UserRole.COURSE_ADMINISTRATOR);
        user5.addRole(UserRole.TEACHER);
        user5.addRole(UserRole.STUDENT);
        user6.addRole(UserRole.COURSE_ADMINISTRATOR);
        user6.addRole(UserRole.STUDENT);
        userList.add(user1);
        userList.add(user2);
        userList.add(user3);
        userList.add(user4);
        userList.add(user5);
        userList.add(user6);

        return userList;
    }
}