package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

        StreamObserver<UserRoleChangeResponse> responseObserver = Mockito.mock(StreamObserver.class);
        ArgumentCaptor<UserRoleChangeResponse> responseCaptor = ArgumentCaptor.forClass(UserRoleChangeResponse.class);

        Mockito.doNothing().when(responseObserver).onNext(Mockito.any());
        Mockito.doNothing().when(responseObserver).onCompleted();

        service.removeRoleFromUser(request, responseObserver);

        Mockito.verify(responseObserver).onNext(responseCaptor.capture());
        UserRoleChangeResponse response = responseCaptor.getValue();

        User updatedUser = repository.findById(user.getId());

        Assertions.assertTrue(response.getIsSuccess());
        Assertions.assertEquals(1, updatedUser.getRoles().size());
    }
}