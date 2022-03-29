package nz.ac.canterbury.seng302.portfolio.service;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;

/**
 * The UserAccountsClientServices class implements the functionality of the services outlined
 * by the user_accounts.proto gRPC contacts. This allows the client to make requests to the server
 * regarding their account.
 *
 * @author Sam Clark
 */
@Service
public class UserAccountsClientService {

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountStub;

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceStub asynchStub;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Sends a request to the UserAccountsServerService containing the id of a user, requesting the users account details.
     *
     * @param request - The request to send to the server, uses the GetUserByIDRequest message type defined by user_accounts.proto
     * @return response - The servers response to the request, which follows the UserResponse message format.
     */
    public UserResponse getUserAccountById(GetUserByIdRequest request) {
        logger.info("SERVICE - send getUserAccountById request to server");
        return userAccountStub.getUserAccountById(request);
    }

    /**
     * Sends a request to the UserAccountServerService to register a new user, with a UserRegisterRequest message
     *
     * @param request - The request for a registration, uses the UserRegisterRequest message type defined in the user_accounts.proto contract
     * @return response - A UserRegisterResponse with the information returned regarding the registration attempt.
     */
    public UserRegisterResponse register(UserRegisterRequest request) {
        logger.info("SERVICE - send register request to server");
        return userAccountStub.register(request);
    }

    /**
     * Sends a request to the UserAccountServerService to edit the details of a user, with a EditUserRequest message
     *
     * @param request -The request for an edit, uses the EditUserRequest message type defined in the user_accounts.proto contract
     * @return response - A EditUserResponse with the information returned regarding the detail editing attempt.
     */
    public EditUserResponse editUser(EditUserRequest request) {
        logger.info("SERVICE - send editUser request to server");
        return userAccountStub.editUser(request);
    }

    /**
     * Sends a request to the UserAccountServerService to change the password of a user, with a ChangePasswordRequest message
     *
     * @param request -The request to change password, uses the ChangePasswordRequest message type defined in the user_accounts.proto contract
     * @return response - A ChangePasswordResponse with the information returned regarding the changing of passwords.
     */
    public ChangePasswordResponse changeUserPassword(ChangePasswordRequest request) {
        logger.info("SERVICE - send changeUserPassword request to server");
        return userAccountStub.changeUserPassword(request);
    }

    /**
     * This function is the server side of a bidirctional stream for sending the photos over gRPC. It calls a function
     * in UserAccountServerService which returns a StreamObserver, that is then used to send the file data.
     * <br>
     * @param photo - A File object containing a photo
     * @param userId - The id of the user
     * @param fileType - The file extension of the photo
     * @throws IOException if reading the photo fails
     */
    public void uploadProfilePhoto(InputStream photo, int userId, String fileType) throws IOException {
        logger.info("Uploading profile photo");
        ArrayList<UploadUserProfilePhotoRequest> requestChunks = new ArrayList<>();

        ProfilePhotoUploadMetadata metadata = ProfilePhotoUploadMetadata.newBuilder()
                .setUserId(userId)
                .setFileType(fileType)
                .build();


        requestChunks.add(UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(metadata)
                .build()
        );
        // Send file, split into 4KiB chunks

        byte[] bytes = new byte[4096];
        int size;
        while ((size = photo.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            requestChunks.add(uploadRequest);
        }
        photo.close();

        ImageResponseStreamObserver responseObserver = new ImageResponseStreamObserver();
        StreamObserver<UploadUserProfilePhotoRequest> requestObserver = asynchStub.uploadUserProfilePhoto(responseObserver);
        responseObserver.initialise(requestObserver);
        responseObserver.sendImage(requestChunks);

    }

    public DeleteUserProfilePhotoResponse deleteUserProfilePhoto (DeleteUserProfilePhotoRequest request) {
        return userAccountStub.deleteUserProfilePhoto(request);
    }

}
