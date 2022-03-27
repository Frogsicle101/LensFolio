package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
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

        repository.save(user);

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
        .setRole(UserRole.TEACHER)
        .setUserId(user.getId())
        .build();

        StreamObserver<UserRoleChangeResponse> observer = new StreamObserver<>() {

            boolean successful;
            boolean message;

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
                assertEquals(1, innerUser.getRoles().size());
            }
        };

        //How do I build these right so I can call the method?
        service.removeRoleFromUser(request, observer);
    }
}