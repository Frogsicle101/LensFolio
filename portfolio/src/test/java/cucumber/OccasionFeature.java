package cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import org.junit.jupiter.api.Assertions;

import javax.naming.InvalidNameException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class OccasionFeature {
    private Project project;
    private Deadline deadline;
    private Milestone milestone;

    @Given("a project exists from {string} to {string}")
    public void a_project_exists_from_start_date_to_end_date(String startDate, String endDate) {
        project = new Project("default", LocalDate.parse(startDate), LocalDate.parse(endDate), "test");
        Assertions.assertNotNull(project);
    }

    @When("a user creates a deadline for {string} with name {string}")
    public void a_user_creates_a_deadline_for_deadline_date_with_name_deadline_name(String deadlineDate, String deadlineName) {
        if (deadlineDate.equals("left blank")) {
            deadlineDate = null;
        } else if (deadlineName.equals("left blank")) {
            deadlineName = null;
        }
        try {
            LocalDateTime parsedDate = LocalDateTime.parse(deadlineDate);
            deadline = new Deadline(project, deadlineName, parsedDate.toLocalDate(), parsedDate.toLocalTime(), 1);
        } catch (DateTimeException | NullPointerException e) {
            deadline = null;
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
    }



    @When("a user creates a milestone for {string} with name {string} and type {int}")
    public void a_user_creates_a_milestone_for_milestone_date_with_name_milestone_name(String milestoneDate, String milestoneName, int type) {
        if (milestoneDate.equals("left blank")) {
            milestoneDate = null;
        } else if (milestoneName.equals("left blank")) {
            milestoneName = null;
        }
        try {
            LocalDate parsedDate = LocalDate.parse(milestoneDate);
            milestone = new Milestone(project, milestoneName, parsedDate, type);
        } catch (DateTimeException | NullPointerException e) {
            milestone = null;
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
    }

    @Then("The deadline exists: {string}")
    public void the_deadline_exists(String deadlineExistsString) {
        boolean deadlineExists = Boolean.parseBoolean(deadlineExistsString);
        assertEquals(deadlineExists, deadline != null);
    }
    @Then("The milestone exists: {string}")
    public void the_milestone_exists(String milestoneExistsString) {
        boolean deadlineExists = Boolean.parseBoolean(milestoneExistsString);
        assertEquals(deadlineExists, milestone != null);
    }
}
