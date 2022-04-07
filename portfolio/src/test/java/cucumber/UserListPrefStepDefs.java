package cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.mock;

public class UserListPrefStepDefs {

    private static AuthState mockedAuthState = mock(AuthState.class);
    private static PrincipalAttributes mockedPrincipalAttributes = mock(PrincipalAttributes.class);

    @Given("I am logged in as {String}")
    public void iAmLoggedInAsUser() {
        // mock the user repository
        // mock logging in
    }

    @When("I sort the list by {String}")
    public void iSortTheListByCategory() {
        // Mock the sorting preferences repository
        // Sort
    }

    @Then("My sorting preference is {String}")
    public void mySortingPreferenceIsPreference() {
        // Check if the preference is correct
    }

    @And("Someone else sorts their list by {String}")
    public void someoneElseSortsTheirListByOtherCategory() {
        // Some predefined user sorts
    }
}
