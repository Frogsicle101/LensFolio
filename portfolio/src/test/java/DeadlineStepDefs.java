import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.Project;

import javax.naming.InvalidNameException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class DeadlineStepDefs {
    private Project project;
    private Deadline deadline;

    @Given("a project exists from {string} to {string}")
    public void aProjectExistsFromStartDateToEndDate(String startDate, String endDate) {
        project = new Project("default", LocalDate.parse(startDate), LocalDate.parse(endDate), "test");

    }

    @When("a user creates a deadline for {string}")
    public void aUserCreatesADeadlineForDeadlineDate(String deadlineDate) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(deadlineDate);
            deadline = new Deadline(project, "test", dateTime.toLocalDate(), dateTime.toLocalTime());
        } catch (DateTimeException e) {
            deadline = null;
        }
    }

    @Then("The deadline exists: {string}")
    public void theDeadlineExistsBoolDeadlineExists(String deadlineExistsString) {
        boolean deadlineExists = Boolean.valueOf(deadlineExistsString);
        assertEquals(deadline != null, deadlineExists);
    }

    @When("a user creates a deadline for {string} with name {string}")
    public void aUserCreatesADeadlineForDeadlineDateWithNameDeadlineName(String deadlineDate, String deadlineName) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(deadlineDate);
            deadline = new Deadline(project, deadlineName, dateTime.toLocalDate(), dateTime.toLocalTime());
        } catch (DateTimeException e) {
            deadline = null;
        }
    }
}
