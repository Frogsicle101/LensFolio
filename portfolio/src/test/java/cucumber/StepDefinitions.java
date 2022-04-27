package cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class containing the step definitions for the account_credited Cucumber feature
 */
public class StepDefinitions {

    @Given("i have created this right")
    public void i_have_created_this_right() {
        ;
    }
    @When("I run my test")
    public void i_run_my_test() {
        ;
    }
    @Then("this should give true")
    public void this_should_give_true() {
        assertEquals(1,1);
    }

}
