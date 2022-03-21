package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyRoleOfUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyRoleOfUserRequestOrBuilder;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRoleChangeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountsServerServiceTest {

    UserAccountsServerService service;
    User user;


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

        ModifyRoleOfUserRequest.Builder request = ModifyRoleOfUserRequest.newBuilder();
        UserRoleChangeResponse.Builder response = UserRoleChangeResponse.newBuilder();
        request.setRole(UserRole.TEACHER);
        request.setUserId(user.getId());
        request.build();
        //How do I build these right so I can call the method?
        //service.removeRoleFromUser(request, response);
    }
}