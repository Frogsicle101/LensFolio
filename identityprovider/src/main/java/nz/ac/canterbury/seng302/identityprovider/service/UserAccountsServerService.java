package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;

@GrpcService
public class UserAccountsServerService extends UserAccountServiceImplBase {


    @Override
    public void getUserAccountById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        UserResponse.Builder reply = UserResponse.newBuilder();
        User user = DatabaseService.getUserfromDatabase(request.getId()); //ToDo This is broken because it should look up by ID

        //Build UserResponse (proto) from User
        reply.setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setNickname(user.getNickname())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPronouns())
                .setEmail(user.getEmail());
//                .setCreated()  ??
//                .setProfileImagePath(user.profileImagePath())
//                .setRoles(user.getRoles())

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }
}
