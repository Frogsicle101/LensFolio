package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * getUserAccountByID follows the gRPC contract and provides the server side service for retrieving
     * user account details from the repository of users.
     *
     * @param request - The GetUserByIDRequest formatted to satisfy the user_accounts.proto gRPC
     * @param responseObserver - used to return the response to the Client side of the service
     */
    @Override
    public void getUserAccountById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        logger.info("SERVICE - Getting user details by Id: " + request.getId());
        UserResponse.Builder reply = UserResponse.newBuilder();
        User user = repository.findById(request.getId());
        logger.info("Sending user details for " + user.getUsername());
        //Build UserResponse (proto) from User
        reply.setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setNickname(user.getNickname())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPronouns())
                .setEmail(user.getEmail())
                .setCreated(user.getAccountCreatedTime())
                /*.setProfileImagePath(user.profileImagePath())*/;


        // To add all the users roles to the response
        ArrayList<UserRole> roles = user.getRoles();
        for (UserRole role : roles) {
            reply.addRoles(role);
        }

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
        logger.info("SERVICE - Registering new user with username " + request.getUsername());
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
                    request.getEmail(),
                    TimeService.getTimeStamp());


            if (repository.findByUsername(user.getUsername()) == null) {
                logger.info("Registration Success - for new user " + request.getUsername());
                repository.save(user);
                reply.setIsSuccess(true)
                        .setNewUserId(user.getId())
                        .setMessage("Account has successfully been registered");
            } else {
                    logger.info("Registration Failure - username " + request.getUsername() + " already in use");
                    reply.setIsSuccess(false);
                    reply.setMessage("Username already in use");
            }

        } catch (io.grpc.StatusRuntimeException e) {
            reply.setIsSuccess(false);
            reply.setMessage("An error occurred registering user from request");
            logger.error("An error occurred registering user from request: " + request + "\n see stack trace below \n");
            logger.error(e.getMessage());
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for editing users, this method attempts to edit the details of a user.
     * <br>
     * This service first attempts to find the user by their id so that they can have their details edited <br>
     *  - If the user can't be found a response message is set to send a failure message to the client <br>
     *  - Otherwise the users details are updated as according to the request.
     *
     * @param request - The gRPC EditUserRequest passed from the client
     * @param responseObserver - Used to return the response to the client side.
     */
    @Transactional
    @Override
    public void editUser(EditUserRequest request, StreamObserver<EditUserResponse> responseObserver) {
        logger.info("SERVICE - Editing details for user with id " + request.getUserId());
        EditUserResponse.Builder response = EditUserResponse.newBuilder();
        // Try to find user by ID
        User userToEdit = repository.findById(request.getUserId());
        if (userToEdit != null) {
            try {
                logger.info("User Edit Success - updated user details for user " + request.getUserId());
                userToEdit.setFirstName(request.getFirstName());
                userToEdit.setMiddleName(request.getMiddleName());
                userToEdit.setLastName(request.getLastName());
                userToEdit.setNickname(request.getNickname());
                userToEdit.setBio(request.getBio());
                userToEdit.setPronouns(request.getPersonalPronouns());
                userToEdit.setEmail(request.getEmail());
                repository.save(userToEdit);
                response.setIsSuccess(true)
                        .setMessage("Successfully updated details for " + userToEdit.getUsername());
            } catch (StatusRuntimeException e) {
                logger.error("An error occurred editing user from request: " + request + "\n See stack trace below \n");
                logger.error(e.getMessage());
                response.setIsSuccess(false)
                        .setMessage("Incorrect current password provided");
            }
        } else {
            logger.info("User Edit Failure - could not find user with id " + request.getUserId());
            response.setIsSuccess(false)
                    .setMessage("Could not find user to edit");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * Follows the gRPC contract for editing users, this method attempts to change the password of a User
     * <br>
     * This service first attempts to find the user by their id so that they can have their password changed <br>
     *  - If the user can't be found a response message is set to send a failure message to the client <br>
     *  - Otherwise the oldPassword is checked against the database to make sure the user knows their old password
     *  before changing <br>
     *    - If this password is correct the password is updated to the new password, otherwise the user is informed
     *    that they have used an incorrect old password.
     *
     * @param request - The gRPC ChangePasswordRequest passed from the client
     * @param responseObserver - Used to return the response to the client side.
     */
    @Transactional
    @Override
    public void changeUserPassword(ChangePasswordRequest request, StreamObserver<ChangePasswordResponse> responseObserver) {
        logger.info("SERVICE - Changing password for user with id" + request.getUserId());
        ChangePasswordResponse.Builder response = ChangePasswordResponse.newBuilder();

        User userToUpdate = repository.findById(request.getUserId());
        if (userToUpdate != null) {
            // User is found, check correct current password provided
            try {
                // encrypt attempted current password to "match" pwhash
                LoginService encryptor = new LoginService();
                String inputPWHash = encryptor.getHash(request.getCurrentPassword(), userToUpdate.getSalt());
                // Check encrypted password against pw hash
                if (userToUpdate.getPwhash().equals(inputPWHash)) {
                    // If password hash matches, update
                    logger.info("Password Change Success - password updated for user " + request.getUserId());
                    userToUpdate.setPwhash(request.getNewPassword());
                    repository.save(userToUpdate);
                    response.setIsSuccess(true)
                            .setMessage("Successfully updated password for " + userToUpdate.getUsername());
                } else {
                    logger.info("Password Change Failure - incorrect old password for " + request.getUserId());
                    // Password hash doesn't match so don't update
                    response.setIsSuccess(false)
                            .setMessage("Incorrect current password provided");
                }
            } catch (StatusRuntimeException e) {
                logger.error("An error occurred changing user password from request: " + request + "\n See stack trace below \n");
                logger.error(e.getMessage());
                response.setIsSuccess(false)
                        .setMessage("An error has occurred while connecting to the database");
            }
        } else {
            logger.info("Password Change Failure - could not find user with id " + request.getUserId());
            response.setIsSuccess(false)
                    .setMessage("Could not find user");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

}
