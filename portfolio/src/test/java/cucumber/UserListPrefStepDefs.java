package cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.controller.UserListController;
import nz.ac.canterbury.seng302.portfolio.userPrefs.UserPrefRepository;
import nz.ac.canterbury.seng302.portfolio.userPrefs.UserPrefs;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserListPrefStepDefs {

    private int testId;

    @Autowired
    private UserPrefRepository repository;

    private UserListController controller = new UserListController(repository);

    @Given("I am logged in as a user with id {int}")
    public void iAmLoggedInAsAUserWithIdUserId(int userId) {
        testId = userId;
        UserPrefs user = new UserPrefs(testId, "name-increasing");
        repository.save(user);
    }

    @When("I sort the list by {String}")
    public void iSortTheListByCategory(String category) {
        controller.selectSortOrder(testId, category);
    }

    @Then("My sorting preference is {String}")
    public void mySortingPreferenceIsPreference(String expectedOrder) {
        UserPrefs user = repository.findByUserId(testId);
        assertEquals(expectedOrder, user.getListSortPref());
    }

    @And("Someone else sorts their list by {String}")
    public void someoneElseSortsTheirListByOtherCategory(String sortOrder) {
        int someOtherId = testId + 8;
        UserPrefs otherUser = new UserPrefs(someOtherId, "name-increasing");
        repository.save(otherUser);
        controller.selectSortOrder(someOtherId, sortOrder);
    }

}
