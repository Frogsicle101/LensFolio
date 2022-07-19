package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

/**
 * The UserAccountsServerService implements the server side functionality of the defined by the
 * user_accounts.proto rpc contracts.
 */
@GrpcService
public class UserAccountsServerService extends UserAccountServiceImplBase {

    /** The repository where Users details are stored */
    @Autowired
    private UserRepository repository;

    @Autowired
    private UrlService urlService;

    @Autowired
    private Environment env;

    @Autowired
    private GroupService groupService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Name Comparator */
    Comparator<User> compareByName = Comparator.comparing((User user) -> (user.getFirstName().toLowerCase() + user.getMiddleName().toLowerCase() + user.getLastName().toLowerCase()));

    /** Username Comparator */
    Comparator<User> compareByUsername = Comparator.comparing(user -> user.getUsername().toLowerCase());

    /** alias Comparator */
    Comparator<User> compareByAlias = Comparator.comparing(user -> user.getNickname().toLowerCase());

    /** role Comparator */
    Comparator<User> compareByRole = (userOne, userTwo) -> {
        ArrayList<UserRole> userOneRoles = userOne.getRoles();
        ArrayList<UserRole> userTwoRoles = userTwo.getRoles();
        Collections.sort(userOneRoles);
        Collections.sort(userTwoRoles);
        return userOneRoles.toString().compareTo(userTwoRoles.toString());
    };


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
        User user = repository.findById(request.getId());
        logger.info("Sending user details for " + user.getUsername());
        UserResponse reply = user.userResponse();

        responseObserver.onNext(reply);
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
                groupService.addGroupMemberByGroupShortName("Non-Group",user.getId());
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
        } catch (Exception e) {
            logger.info("An unexpected error occurred when trying to add the new user:\n" + e.getMessage());
            reply.setIsSuccess(false)
                    .setMessage("An Unexpected error occurred");
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for editing users, this method attempts to edit the details of a user.
     *
     * This service first attempts to find the user by their id so that they can have their details edited
     *  - If the user can't be found a response message is set to send a failure message to the client
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
     *
     * This service first attempts to find the user by their id so that they can have their password changed
     *  - If the user can't be found a response message is set to send a failure message to the client
     *  - Otherwise the oldPassword is checked against the database to make sure the user knows their old password
     *  before changing
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
                            .setMessage("Successfully updated details for " + userToUpdate.getUsername());
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


    /**
     * The gRPC implementation of bidirectional streaming used to receive uploaded user profile images.
     * <br>
     * The server creates a stream observer and defines its actions when the client calls the OnNext, onError and
     * onComplete methods.
     *
     * @param responseObserver - Contains an observer, which the Client side defines the implementation for. This allows
     *                           client side actions to be called from the server side. E.g., if bytes have been
     *                           received from the client successfully, the server will call
     *                           responseObserver.onNext(FileUploadStatusResponse) to inform the client to send more.
     *
     * @return requestObserver - Contains an observer defined by the server, so that the client can call server side
     *                           actions. Therefore, this method defines the servers actions when the client calls them.
     */
    @Override
    public StreamObserver<UploadUserProfilePhotoRequest> uploadUserProfilePhoto(StreamObserver<FileUploadStatusResponse> responseObserver) {
        return new ImageRequestStreamObserver(responseObserver, repository, env);
    }


    @Override
    public void deleteUserProfilePhoto(DeleteUserProfilePhotoRequest request, StreamObserver<DeleteUserProfilePhotoResponse> responseObserver) {
        DeleteUserProfilePhotoResponse.Builder response = DeleteUserProfilePhotoResponse.newBuilder();
        try {
            int id = request.getUserId();
            User user = repository.findById(id);
            boolean deleteSuccess = user.deleteProfileImage(env);
            response.setIsSuccess(deleteSuccess);
        } catch (Exception exception) {
            response.setIsSuccess(false);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for editing users, this method attempts to add a role to a User.
     *
     * This service first attempts to find the user by their id so that they can have their role changed <br>
     *  - If the user can't be found a response message is set to send a failure message to the client <br>
     *  - Otherwise the role to be added is checked against the user's current roles to prevent duplication, then the
     *  role is added if it's unique for the user.
     *
     * @param request - The gRPC ModifyRoleOfUserRequest passed from the client
     * @param responseObserver - Used to return the response to the client side.
     */
    @Override
    public void addRoleToUser(ModifyRoleOfUserRequest request, StreamObserver<UserRoleChangeResponse> responseObserver) {
        logger.info("Service - Adding role " + request.getRole() + " to user " + request.getUserId());
        UserRoleChangeResponse.Builder response = UserRoleChangeResponse.newBuilder();

        User userToUpdate = repository.findById(request.getUserId());
        if (userToUpdate != null) {
            try {
                if (!userToUpdate.getRoles().contains(request.getRole())) {
                    userToUpdate.addRole(request.getRole());
                    repository.save(userToUpdate);
                    if (request.getRole() == UserRole.TEACHER) {
                        groupService.addGroupMemberByGroupShortName("Teachers", userToUpdate.getId());
                    }
                    response.setIsSuccess(true)
                            .setMessage(MessageFormat.format("Successfully added role {0} to user {1}",
                                    request.getRole(), userToUpdate.getId()));
                } else {
                    response.setIsSuccess(false)
                            .setMessage("User already has that role");
                }
            } catch (Exception e){
                logger.info("An unexpected error occurred when trying to add a role to user:\n" + e.getMessage());
                response.setIsSuccess(false)
                        .setMessage("An Unexpected error occurred");
            }
        } else {
            response.setIsSuccess(false)
                    .setMessage("Could not find user");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for editing users, this method attempts to remove a role from a User.
     *
     * This service first attempts to find the user by their id so that they can have their role changed
     *  - If the user can't be found a response message is set to send a failure message to the client
     *
     *  - Otherwise the role to be removed is checked against the user's current roles to prevent deleting a role
     *  that doesn't exist.
     *
     *  - Finally, we attempt to delete the role. If the user has 1 - or somehow no roles (which should not happen) -
     *  then an exception gets thrown, because a user should always have at least 1 role. We catch this exception
     *  and send a failure message.
     *
     * @param request - The gRPC ModifyRoleOfUserRequest passed from the client
     * @param responseObserver - Used to return the response to the client side.
     */
    @Override
    public void removeRoleFromUser(ModifyRoleOfUserRequest request, StreamObserver<UserRoleChangeResponse> responseObserver) {
        logger.info("Service - Removing role " + request.getRole() +  " from user " + request.getUserId());
        UserRoleChangeResponse.Builder response = UserRoleChangeResponse.newBuilder();

        User userToUpdate = repository.findById(request.getUserId());
        if (userToUpdate != null) {
            //We've found the user!
            try {
                userToUpdate.deleteRole(request.getRole());
                repository.save(userToUpdate);
                logger.info("Role Removal Success - removed " + request.getRole()
                        + " from user " + request.getUserId());
                if (request.getRole().equals(UserRole.TEACHER)){
                    groupService.removeGroupMembersByGroupShortName("Teachers", userToUpdate.getId());
                }
                response.setIsSuccess(true)
                        .setMessage(MessageFormat.format("Successfully removed role {0} from user {1}",
                                request.getRole(), userToUpdate.getId()));
            } catch (IllegalStateException e) {
                //The user has only one role - we can't delete it!
                logger.info("Role Removal Failure - user " + request.getUserId()
                        + " has 1 role. Users cannot have 0 roles");
                response.setIsSuccess(false)
                        .setMessage("The user can't have zero roles");
            } catch (Exception e) {
                logger.info(e.getMessage());
                response.setIsSuccess(false)
                        .setMessage("An Unexpected error occurred");
            }
        } else {
            //Here, we couldn't find the user, so we do not succeed.
            logger.info("Role Removal Failure - could not find user " + request.getUserId());
            response.setIsSuccess(false)
                    .setMessage("Could not find user");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for retrieving the paginated users. Does this by sorting a list of all the users based
     * on what was requested and then looping through to add the specific page of users to the response
     *
     * @param request the GetPaginatedUsersRequest passed through from the client service
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getPaginatedUsers(GetPaginatedUsersRequest request, StreamObserver<PaginatedUsersResponse> responseObserver) {
        PaginatedUsersResponse.Builder reply = PaginatedUsersResponse.newBuilder();
        List<User> allUsers = (List<User>) repository.findAll();
        String sortMethod = request.getOrderBy();

        switch (sortMethod) {
            case "roles-increasing" -> allUsers.sort(compareByRole);
            case "roles-decreasing" -> {
                allUsers.sort(compareByRole);
                Collections.reverse(allUsers);
            }
            case "username-increasing" -> allUsers.sort(compareByUsername);
            case "username-decreasing" -> {
                allUsers.sort(compareByUsername);
                Collections.reverse(allUsers);
            }
            case "aliases-increasing" -> allUsers.sort(compareByAlias);
            case "aliases-decreasing" -> {
                allUsers.sort(compareByAlias);
                Collections.reverse(allUsers);
            }
            case "name-decreasing" -> {
                allUsers.sort(compareByName);
                Collections.reverse(allUsers);
            }
            default -> allUsers.sort(compareByName);
        }
        //for each user up to the limit or until all the users have been looped through, add to the response
        for (int i = request.getOffset(); ((i - request.getOffset()) < request.getLimit()) && (i < allUsers.size()); i++) {
            reply.addUsers(allUsers.get(i).userResponse());
        }
        reply.setResultSetSize(allUsers.size());
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }
}
