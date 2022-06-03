package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.groups.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserAccountsServerServiceTest {

    @Autowired
    UserAccountsServerService service;

    @Autowired
    UserRepository repository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    private UrlService urlService;

    User user;

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountStub;

    @BeforeEach
    void setUp() {
        user = new User(
                "test",
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

        repository.deleteAll();
        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(user.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> observer = new StreamObserver<>() {

            boolean successful;
            String message;

            @Override
            public void onNext(UserRoleChangeResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertTrue(successful);
                User innerUser = repository.findById(user.getId());
                assertFalse(innerUser.getRoles().contains(UserRole.TEACHER));
            }
        };

        service.removeRoleFromUser(request, observer);
    }


    @Test
    void removeExistingRoleFromUserNoUserOfThatId() {
        //Add some roles to the user
        user.addRole(UserRole.STUDENT);
        user.addRole(UserRole.TEACHER);

        repository.deleteAll();
        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(-1)
                .build();

        StreamObserver<UserRoleChangeResponse> observer = new StreamObserver<>() {

            boolean successful;
            String message;

            @Override
            public void onNext(UserRoleChangeResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertFalse(successful);
                assertEquals("Could not find user", message);
                User innerUser = repository.findById(user.getId());
                assertTrue(innerUser.getRoles().contains(UserRole.TEACHER));
            }
        };

        service.removeRoleFromUser(request, observer);
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

        StreamObserver<UserRegisterResponse> observer = new StreamObserver<>() {

            boolean successful;
            int userId;
            String message;

            @Override
            public void onNext(UserRegisterResponse value) {
                successful = value.getIsSuccess();
                userId = value.getNewUserId();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertTrue(successful);
                assertTrue(repository.existsById(userId));
            }
        };

        service.register(request.build(), observer);
    }


    @Test
    void registerNewUserUsernameInUse() {
        repository.deleteAll();
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

        StreamObserver<UserRegisterResponse> observer = new StreamObserver<>() {

            boolean successful;
            int userId;
            String message;

            @Override
            public void onNext(UserRegisterResponse value) {
                successful = value.getIsSuccess();
                userId = value.getNewUserId();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertFalse(successful);
                assertFalse(repository.existsById(userId));
                assertEquals("Username already in use", message);
            }
        };

        service.register(request.build(), observer);
    }


    @Test
    void getUserAccountById() {
        repository.deleteAll();
        repository.save(user);

        GetUserByIdRequest.Builder request = GetUserByIdRequest.newBuilder();
        request.setId(user.getId());

        UserResponse.Builder expectedObject = UserResponse.newBuilder();

        expectedObject.setUsername(user.getUsername())
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

        StreamObserver<UserResponse> observer = new StreamObserver<>() {

            UserResponse response;

            @Override
            public void onNext(UserResponse value) {
                response = value;
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertEquals(expectedObject.build(), response);
            }
        };

        service.getUserAccountById(request.build(), observer);
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

        StreamObserver<EditUserResponse> observer = new StreamObserver<>() {

            boolean successful;
            String message;

            @Override
            public void onNext(EditUserResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertTrue(successful);
                assertEquals("Johnny", repository.findById(user.getId()).getFirstName());
            }
        };

        service.editUser(request.build(), observer);
    }


    @Test
    void editUserNoUserOfThatId() {
        repository.deleteAll();
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

        StreamObserver<EditUserResponse> observer = new StreamObserver<>() {

            boolean successful;
            String message;

            @Override
            public void onNext(EditUserResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertFalse(successful);
                assertEquals("Could not find user to edit", message);
                assertEquals(user.getFirstName(), repository.findById(user.getId()).getFirstName());
            }
        };

        service.editUser(request.build(), observer);
    }


    @Test
    void changeUserPassword() {
        repository.deleteAll();
        repository.save(user);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(user.getId())
                .setCurrentPassword("password")
                .setNewPassword("SuperSecurePassword");

        String salt = user.getSalt();
        LoginService encryptor = new LoginService();
        String expectedPassword = encryptor.getHash(request.getNewPassword(), salt);


        StreamObserver<ChangePasswordResponse> observer = new StreamObserver<>() {
            boolean successful;
            String message;

            @Override
            public void onNext(ChangePasswordResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertTrue(successful);
                assertEquals(expectedPassword, repository.findById(user.getId()).getPwhash());
            }
        };

        service.changeUserPassword(request.build(), observer);
    }


    @Test
    void changeUserPasswordIncorrectCurrentPassword() {
        repository.deleteAll();
        repository.save(user);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(user.getId())
                .setCurrentPassword("is this my password?")
                .setNewPassword("SuperSecurePassword");

        String expectedPassword = user.getPwhash();

        StreamObserver<ChangePasswordResponse> observer = new StreamObserver<>() {
            boolean successful;
            String message;

            @Override
            public void onNext(ChangePasswordResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertFalse(successful);
                assertEquals("Incorrect current password provided" , message);
                assertEquals(expectedPassword, repository.findById(user.getId()).getPwhash());
            }
        };

        service.changeUserPassword(request.build(), observer);
    }


    @Test
    void changeUserPasswordNoUserOfThatId() {
        repository.deleteAll();
        repository.save(user);

        ChangePasswordRequest.Builder request = ChangePasswordRequest.newBuilder();
        request.setUserId(-1)
                .setCurrentPassword("password")
                .setNewPassword("SuperSecurePassword");

        String expectedPassword = user.getPwhash();

        StreamObserver<ChangePasswordResponse> observer = new StreamObserver<>() {
            boolean successful;
            String message;

            @Override
            public void onNext(ChangePasswordResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertFalse(successful);
                assertEquals("Could not find user" , message);
                assertEquals(expectedPassword, repository.findById(user.getId()).getPwhash());
            }
        };

        service.changeUserPassword(request.build(), observer);
    }


    @Test
    void addRoleToUser() {
        repository.deleteAll();
        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(user.getId())
                .build();

        StreamObserver<UserRoleChangeResponse> observer = new StreamObserver<>() {

            boolean successful;
            String message;

            @Override
            public void onNext(UserRoleChangeResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertTrue(successful);
                User innerUser = repository.findById(user.getId());
                assertTrue(innerUser.getRoles().contains(UserRole.TEACHER));
            }
        };

        service.addRoleToUser(request, observer);
    }


    @Test
    void addRoleToUserNoUserOfThatId() {
        repository.deleteAll();
        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                .setRole(UserRole.TEACHER)
                .setUserId(-1)
                .build();

        StreamObserver<UserRoleChangeResponse> observer = new StreamObserver<>() {

            boolean successful;
            String message;

            @Override
            public void onNext(UserRoleChangeResponse value) {
                successful = value.getIsSuccess();
                message = value.getMessage();
            }

            @Override
            public void onError(Throwable t) {
                fail(t.getMessage());
            }

            @Override
            public void onCompleted() {
                assertFalse(successful);
                assertEquals("Could not find user", message);
                User innerUser = repository.findById(user.getId());
                assertFalse(innerUser.getRoles().contains(UserRole.TEACHER));
            }
        };

        service.removeRoleFromUser(request, observer);
    }
}