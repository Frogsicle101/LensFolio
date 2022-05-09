package cucumber.User_Profile_Photos;

import com.google.protobuf.ByteString;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.ImageRequestStreamObserver;
import nz.ac.canterbury.seng302.identityprovider.service.TimeService;
import nz.ac.canterbury.seng302.identityprovider.service.UrlService;
import nz.ac.canterbury.seng302.shared.identityprovider.ProfilePhotoUploadMetadata;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProfilePhotoStepDefinitions {

    @MockBean
    UserRepository repository = mock(UserRepository.class);

    private MockImageResponseStreamObserver mockImageResponseStreamObserver = new MockImageResponseStreamObserver();

    private Environment mockEnv;

    private UrlService urlService;

    private User user;

    private int userId;

    private URL receivedImagePath;

    private URL expectedImagePath;

    @Before
    public void setup() {
        urlService = new UrlService();

        ReflectionTestUtils.setField(urlService, "env", mockEnv);

        mockEnv = mock(Environment.class);
        when(mockEnv.getProperty("photoLocation", "src/main/resources/profile-photos/"))
                .thenReturn("src/main/resources/profile-photos/");

        when(mockEnv.getProperty("protocol", "http")).thenReturn("http");
        when(mockEnv.getProperty("hostName", "localhost")).thenReturn("localhost");
        when(mockEnv.getProperty("port", "9001")).thenReturn("9001");
        when(mockEnv.getProperty("rootPath", "")).thenReturn("");

        ReflectionTestUtils.setField(urlService, "env", mockEnv);
    }

    @Given("I am logged in as user id {int}")
    public void i_am_logged_in_as_user_id(int userId) {
        user = new User(
                "test",
                "password",
                "FirstName",
                "MiddleName",
                "LastName",
                "Nick",
                "This is a bio",
                "He/Him",
                "test@example.com",
                TimeService.getTimeStamp()
        );
        this.userId = userId;

        when(repository.findById(userId)).thenReturn(user);
    }

    @Given("I have no profile photo")
    public void i_have_no_profile_photo() {
        user.deleteProfileImage(mockEnv);
    }


    @When("I request my profile photo Image")
    public void i_request_my_profile_photo_image() {
        receivedImagePath = urlService.getProfileURL(user);
    }


    @Then("I receive the default profile photo icon")
    public void i_receive_the_default_profile_photo_icon() throws MalformedURLException {
        expectedImagePath = new URL("http", "localhost", 9001, "profile/default.png");
        assertEquals(expectedImagePath, receivedImagePath);
    }


    @When("I change my profile photo")
    public void i_change_my_profile_photo() {
        try {
            URL resource = ProfilePhotoStepDefinitions.class.getResource("/testProfileImage.jpg");
            String imagePath = Paths.get(resource.toURI()).toFile().getAbsolutePath();

            uploadTestProfileImage(imagePath, "jpg");
            expectedImagePath = new URL("http", "localhost", 9001, "/profile/" + 1 + ".jpg");
        } catch (MalformedURLException exception) {
            fail("Invalid URL setting");
        } catch (IOException exception) {
            fail("Failed to upload the new image");
        } catch (URISyntaxException exception) {
            fail("Failed to Convert image path");
        } catch (NullPointerException exception) {
            fail("Failed to find test image resource");
        }
    }


    @Then("I receive the profile photo for id {int}")
    public void i_receive_the_profile_photo_for_id(int userId) {
        receivedImagePath = urlService.getProfileURL(repository.findById(userId));
        assertEquals(expectedImagePath, receivedImagePath);
    }



    private void uploadTestProfileImage(String newImage, String fileType) throws IOException {
        when(repository.save(user)).thenReturn(null);

        ArrayList<UploadUserProfilePhotoRequest> requestChunks = new ArrayList<>();

        ProfilePhotoUploadMetadata metadata = ProfilePhotoUploadMetadata.newBuilder()
                .setUserId(userId)
                .setFileType(fileType)
                .build();


        requestChunks.add(UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(metadata)
                .build()
        );

        InputStream photo = new BufferedInputStream(new FileInputStream(newImage));

        byte[] bytes = new byte[4096];
        int size;
        while ((size = photo.read(bytes)) > 0){
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            requestChunks.add(uploadRequest);
        }
        photo.close();

        StreamObserver<UploadUserProfilePhotoRequest> requestObserver = new ImageRequestStreamObserver(mockImageResponseStreamObserver, repository, mockEnv);
        mockImageResponseStreamObserver.initialise(requestObserver);
        mockImageResponseStreamObserver.sendImage(requestChunks);
    }

}
