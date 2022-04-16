package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.ProfilePhotoUploadMetadata;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Defines the StreamObserver<UploadUserProfilePhotoRequest> implementation used by the UserAccountsServerService for
 * uploading images.
 * <br>
 * @author Sam Clark
 */
public class ImageRequestStreamObserver implements StreamObserver<UploadUserProfilePhotoRequest> {

    Logger logger = LoggerFactory.getLogger(this.getClass());


    private int userId;
    private String fileType;
    private ByteString bytes;
    private final StreamObserver<FileUploadStatusResponse> responseObserver;
    private final UserRepository userRepository;

    public ImageRequestStreamObserver (StreamObserver<FileUploadStatusResponse> responseObserver, UserRepository userRepository) {
        this.responseObserver = responseObserver;
        this.userRepository = userRepository;
    }

    /**
     * should be called by the client when they are sending data to the server. The first chunk should be
     * metadata, and every chunk following should be fileContent. On reception calls the responseObservers
     * onNext method to request the next chunk.
     * <br>
     * @param request - the UploadUserProfilePhotoRequest chunk being sent to the server
     */
    @Override
    public void onNext(UploadUserProfilePhotoRequest request) {
//  --------------------------------- Check if the first "packet" is the metadata --------------------------------------
        if (request.getUploadDataCase() == UploadUserProfilePhotoRequest.UploadDataCase.METADATA) {
            ProfilePhotoUploadMetadata metadata = request.getMetaData();
            logger.info("Received image metadata: " + metadata);

            // Metadata received, create new image and tell client with PENDING status
            userId = metadata.getUserId();
            fileType = metadata.getFileType();
            bytes = ByteString.EMPTY;
            // Update client that metadata received
            responseObserver.onNext(FileUploadStatusResponse.newBuilder()
                    .setStatus(FileUploadStatus.PENDING)
                    .setMessage("Received image metadata: " + metadata)
                    .build()
            );
//  ---------------------------- Otherwise the incoming content must be file chunks ------------------------------------
        } else {
            ByteString fileContent = request.getFileContent();
            logger.info("Received image chunk of size: " + fileContent.size());

            // If the metadata wasn't received first as error will occur
            if (bytes == null) {
                logger.error("Image metadata data not sent before transfer");
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Image Content sent before metadata")
                                .asRuntimeException()
                );
            } else {
                bytes = bytes.concat(fileContent);
                // Update client that contents received
                responseObserver.onNext(FileUploadStatusResponse.newBuilder()
                        .setStatus(FileUploadStatus.IN_PROGRESS)
                        .setMessage("Received " + fileContent.size() + " bytes of image data")
                        .build()
                );

            }
        }
    }



    /**
     * has little effect for the server, as it is more crucial for the client, however it informs the server
     * to drop the content received as the transfer was unsuccessful and calls responseObserver.onNext with an
     * FAILED FileUploadStatus.
     * <br>
     * @param throwable - the error thrown when the error occurred
     */
    @Override
    public void onError(Throwable throwable) {
        responseObserver.onNext(FileUploadStatusResponse.newBuilder()
                .setStatus(FileUploadStatus.FAILED)
                .setMessage("An error has occurred")
                .build());
        logger.error(throwable.getMessage());
    }

    /**
     * When called the server can save the data received return a SUCCESS FileUploadStatus calling onNext and
     * onComplete to tell the client that the server has saved the image.
     */
    @Override
    public void onCompleted() {
        FileUploadStatusResponse.Builder response = FileUploadStatusResponse.newBuilder();
        saveImageToGallery();
        response.setStatus(FileUploadStatus.SUCCESS)
                .setMessage("COMPLETE: Successfully transferred bytes");


        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * Called on a successful image transfer in onComplete method. This method takes the imageContents and saves it to
     * a file in the user's directory with an image type of that sent in metadata.
     */
    private void saveImageToGallery() {
        try {
            FileOutputStream out = new FileOutputStream(
                    "src/main/resources/profile-photos/" + userId + "." + fileType
            );

            String path = "/profile/" + userId + "." + fileType;
            User user = userRepository.findById(userId);
            user.setProfileImagePath(path);
            userRepository.save(user);

            bytes.writeTo(out);
            out.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

