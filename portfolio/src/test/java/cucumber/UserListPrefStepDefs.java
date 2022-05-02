package cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.controller.UserListController;
import nz.ac.canterbury.seng302.portfolio.userPrefs.UserPrefRepository;
import nz.ac.canterbury.seng302.portfolio.userPrefs.UserPrefs;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class UserListPrefStepDefs {

    private static AuthState mockedAuthState = mock(AuthState.class);
    private static PrincipalAttributes mockedPrincipalAttributes = mock(PrincipalAttributes.class);

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
    public void iSortTheListByCategory(String sortOrder) {
        controller.selectSortOrder(testId, sortOrder);
    }

    @Then("My sorting preference is {String}")
    public void mySortingPreferenceIsPreference(String expectedOrder) {
        UserPrefs user = repository.findByUserId(testId);
        assertEquals(expectedOrder, user.getListSortPref());
    }

    @And("Someone else sorts their list by {String}")
    public void someoneElseSortsTheirListByOtherCategory() {
        // Some predefined user sorts
    }


}
