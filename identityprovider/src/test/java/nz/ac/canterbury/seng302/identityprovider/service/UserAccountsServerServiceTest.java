package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountsServerServiceTest {

    UserAccountsServerService service;
    User user;

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountStub;

    @BeforeEach
    void setUp() {
        service = new UserAccountsServerService();
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

        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
        .setRole(UserRole.TEACHER)
        .setUserId(user.getId())
        .build();


        UserRoleChangeResponse response;
        //How do I build these right so I can call the method?
        response = userAccountStub.removeRoleFromUser(request);

        assertTrue(response.getIsSuccess());
        assertEquals(1, user.getRoles().size());
    }
}