package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * The UserAccountsServerService implements the server side functionality of the defined by the
 * user_accounts.proto rpc contracts.
 *
 * @author Sam Clark
 */
@GrpcService
public class UserAccountsServerService extends UserAccountServiceImplBase {

    /** The repository where Users details are stored */
    @Autowired
    private UserRepository repository;



    /**
     * getUserAccountByID follows the gRPC contract and provides the server side service for retrieving
     * user account details from the repository of users.
     *
     * @param request - The GetUserByIDRequest formatted to satisfy the user_accounts.proto gRPC
     * @param responseObserver - used to return the response to the Client side of the service
     */
    @Override
    public void getUserAccountById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        UserResponse.Builder reply = UserResponse.newBuilder();
        User user = repository.findById(request.getId());

        //Build UserResponse (proto) from User
        reply.setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
//                .setMiddleName(user.getMiddleName())
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

    /**
     * Follows the gRPC contract and provides the server side service for registering new users, adding them to the database
     *
     * @param request - A UserRegisterRequest formatted to satisfy the user_accounts.proto contract
     * @param responseObserver - Used to return the response to the client side.
     */
    @Override
    public void register(UserRegisterRequest request, StreamObserver<UserRegisterResponse> responseObserver) {
        UserRegisterResponse.Builder reply = UserRegisterResponse.newBuilder();
        // Untested
        try {
            User user = new User(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getMiddleName(),
                    request.getLastName(),
                    request.getNickname(),
                    request.getBio(),
                    request.getPersonalPronouns(),
                    request.getEmail()
            );

            if (repository.findByUsername(user.getUsername()) == null) {
                repository.save(user);
                reply.setIsSuccess(true)
                        .setNewUserId(user.getId())
                        .setMessage("Your account has successfully been registered");
            } else {
                reply.setIsSuccess(false);
                reply.setMessage("Username already in use");
            }

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | io.grpc.StatusRuntimeException e) {
            e.printStackTrace();
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }


    @Override
    public void editUser(EditUserRequest request, StreamObserver<EditUserResponse> responseObserver) {
        super.editUser(request, responseObserver);
    }

    /*
       string FirstName = 2;
   string MiddleName = 3;
   string LastName = 4;
   string Nickname = 5;
   string Bio = 6;
   string PersonalPronouns = 7;
   string Email = 8;
     */
}
