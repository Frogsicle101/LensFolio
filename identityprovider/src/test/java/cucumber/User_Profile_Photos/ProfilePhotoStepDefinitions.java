package cucumber.User_Profile_Photos;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.ProfilePhotoService;
import nz.ac.canterbury.seng302.identityprovider.service.TimeService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProfilePhotoStepDefinitions {

    @MockBean
    @Autowired
    UserRepository repository = mock(UserRepository.class);

    User user;

    int userId;

    URL path;

    URL testImagePath;

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
        user.deleteProfileImage();
    }
    @When("I request my profile photo Image")
    public void i_request_my_profile_photo_image() {
        path = user.getProfileImagePath();
    }
    @Then("I receive the default profile photo icon")
    public void i_receive_the_default_profile_photo_icon() throws MalformedURLException {
        URL expectedPath = new URL("http", "localhost", 9001, "profile/default.png");

        assertEquals(expectedPath, path);
    }

    @When("I change my profile photo")
    public void i_change_my_profile_photo() {
        try {
            testImagePath = new URL("http", "localhost", 9001, "/profile/" + 1 + ".jpg");
        } catch (MalformedURLException exception) {
            fail("Invalid URL setting");
        }
        ProfilePhotoService profilePhotoService = new ProfilePhotoService();
        profilePhotoService.setRepository(repository);
        profilePhotoService.updateProfileImage(userId, testImagePath);
    }

    @Then("I receive the profile photo for id {int}")
    public void i_receive_the_profile_photo_for_id(int userId) {
        User user = repository.findById(userId);
        URL receivedFilePath = user.getProfileImagePath();
        assertEquals(testImagePath, receivedFilePath);
    }

}
