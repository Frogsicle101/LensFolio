package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
public class MilestoneController {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MilestoneController(ProjectRepository projectRepository, MilestoneRepository milestoneRepository) {
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
    }


    /**
     * Mapping for a put request to add milestone.
     * The method first parses a date string that is passed as request parameters.in a format called ISO_DATE_TIME.
     * The parsers converts them it that to the standard LocalDateTime format that we use.
     * <p>
     * The project is then grabbed from the repository by its ID.
     * If the project can't be found, it throws an EntityNotFoundException
     * <p>
     * The Milestone is then created with the parameters passed, and saved to the milestone repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param projectId id of project to add milestone to.
     * @param name      Name of milestone.
     * @param end       date of the end of the milestone.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addMilestone")
    public ResponseEntity<String> addMilestone(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "milestoneName") String name,
            @RequestParam(value = "milestoneEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfOccasion") int typeOfOccasion
    ) {
        try {
            LocalDate milestoneEnd = LocalDate.parse(end);

            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            Milestone milestone = new Milestone(project, name, milestoneEnd, typeOfOccasion);
            milestoneRepository.save(milestone);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException err) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
