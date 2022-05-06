package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.InvalidNameException;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@RestController
public class DeadlineController {

    private final ProjectRepository projectRepository;
    private final DeadlineRepository deadlineRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeadlineController(ProjectRepository projectRepository, DeadlineRepository deadlineRepository) {
        this.projectRepository = projectRepository;
        this.deadlineRepository = deadlineRepository;
    }

    /**
     * Mapping for a put request to add a deadline.
     * The method first parses a date and time string that is passed as a request parameter.
     * The parser converts it to the standard LocalDate format and a LocalTime format
     * <p>
     * The project is then grabbed from the repository by its ID.
     * If the project can't be found, it throws an EntityNotFoundException
     * <p>
     * The deadline is then created with the parameters passed, and saved to the deadline repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param projectId id of project to add deadline to.
     * @param name      Name of milestone.
     * @param dateEnd   date of the end of the deadline.
     * @param timeEnd   time of the end of the deadline
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addDeadline")
    public ResponseEntity<String> addDeadline(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "deadlineName") String name,
            @RequestParam(value = "deadlineDateEnd") String dateEnd,
            @RequestParam(value = "deadlineTimeEnd") String timeEnd,
            @RequestParam(defaultValue = "1", value = "typeOfOccasion") int typeOfOccasion
    ) {
        try {
            LocalDate deadlineEndDate = LocalDate.parse(dateEnd);
            LocalTime deadlineEndTime = LocalTime.parse(timeEnd);

            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            Deadline deadline = new Deadline(project, name, deadlineEndDate, deadlineEndTime, typeOfOccasion);
            deadlineRepository.save(deadline);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException | InvalidNameException err) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mapping for a post request to edit a deadline.
     * The method first gets the deadline from the repository. If the deadline cannot be retrieved, it throws an EntityNotFound exception.
     * <p>
     * The method then parses a date string and a time string that is passed as a request parameter.
     * The parser converts it to the standard LocalDate format.
     * <p>
     * The deadline is then edited with the parameters passed, and saved to the deadline repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param deadlineId the ID of the deadline being edited.
     * @param name the new name of the deadline.
     * @param dateEnd the new date of the deadline.
     * @param timeEnd the new time of the deadline
     * @param typeOfOccasion the new type of the deadline.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/editDeadline")
    public ResponseEntity editDeadline(
            @RequestParam(value = "deadlineId") UUID deadlineId,
            @RequestParam(value = "deadlineName") String name,
            @RequestParam(value = "deadlineDate") String dateEnd,
            @RequestParam(value = "deadlineTime") String timeEnd,
            @RequestParam(defaultValue = "1", value = "typeOfMilestone") int typeOfOccasion
    ) {
        try {
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow(() -> new EntityNotFoundException(
                    "Deadline with id " + deadlineId + " was not found"
            ));

            LocalDate deadlineEndDate = LocalDate.parse(dateEnd);
            LocalTime deadlineEndTime = LocalTime.parse(timeEnd);

            deadline.setName(name);
            deadline.setEndDate(deadlineEndDate);
            deadline.setEndTime(deadlineEndTime);
            deadline.setType(typeOfOccasion);
            deadlineRepository.save(deadline);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException err) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }


}
