package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.shared.identityprovider.ProfilePhotoUploadMetadata;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Defines the StreamObserver<UploadUserProfilePhotoRequest> implementation used by the UserAccountsServerService for
 * uploading images.
 * <br>
 * @author Sam Clark
 */
public class ImageRequestStreamObserver implements StreamObserver<UploadUserProfilePhotoRequest> {

    private int userId;
    private String fileType;
    private ByteArrayOutputStream imageContent;
    private final StreamObserver<FileUploadStatusResponse> responseObserver;

    public ImageRequestStreamObserver (StreamObserver<FileUploadStatusResponse> responseObserver) {
        this.responseObserver = responseObserver;
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
            System.out.println("Received image metadata: " + metadata);

            // Metadata received, create new image and tell client with PENDING status
            userId = metadata.getUserId();
            fileType = metadata.getFileType();
            imageContent = new ByteArrayOutputStream();
            // Update client that metadata received
            responseObserver.onNext(FileUploadStatusResponse.newBuilder()
                    .setStatus(FileUploadStatus.PENDING)
                    .setMessage("Received image metadata: " + metadata)
                    .build()
            );
//  ---------------------------- Otherwise the incoming content must be file chunks ------------------------------------
        } else {
            ByteString fileContent = request.getFileContent();
            System.out.println("Received image chunk of size: " + fileContent.size());

            // If the metadata wasn't received first as error will occur
            if (imageContent == null) {
                System.out.println("Image metadata data not sent before transfer");
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Image Content sent before metadata")
                                .asRuntimeException()
                );
            } else {
                try {
                    fileContent.writeTo(imageContent);
                    // Update client that contents received
                    responseObserver.onNext(FileUploadStatusResponse.newBuilder()
                            .setStatus(FileUploadStatus.IN_PROGRESS)
                            .setMessage("Received " + fileContent.size() + " bytes of image data")
                            .build()
                    );
                } catch (IOException e) {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Failed to write chunks: " + fileContent)
                            .asRuntimeException());
                }
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
        System.out.println(throwable.getMessage());
    }

    /**
     * When called the server can save the data received return a SUCCESS FileUploadStatus calling onNext and
     * onComplete to tell the client that the server has saved the image.
     */
    @Override
    public void onCompleted() {
        int imageSize = imageContent.size();
        // ToDo Implement saving of (userId, filetype, imageContents)
        FileUploadStatusResponse.Builder response = FileUploadStatusResponse.newBuilder();
        try {
            saveImageToGallery();
            response.setStatus(FileUploadStatus.SUCCESS)
                    .setMessage("COMPLETE: Successfully transferred " + imageSize + " bytes");
        } catch (IOException exception) {
            response.setStatus(FileUploadStatus.FAILED)
                    .setMessage("FAILURE: Failed to save image.");
        }


        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    /**
     * Called on a successful image transfer in onComplete method. This method takes the imageContents and saves it to
     * a file in the user's directory with an image type of that sent in metadata.
     *
     * @throws IOException -  Throws an IO Exception if either the writeTo method fails or the close method
     */
    private void saveImageToGallery() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("gallery/" + userId + "/profile" + fileType);
        imageContent.writeTo(fileOutputStream);
        fileOutputStream.close();
    }
}
