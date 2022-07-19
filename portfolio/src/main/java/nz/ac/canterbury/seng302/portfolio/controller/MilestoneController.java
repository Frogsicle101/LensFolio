package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.RegexPatterns;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.persistence.EntityNotFoundException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class MilestoneController {

    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RegexPatterns regexPatterns = new RegexPatterns();

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    public MilestoneController(ProjectRepository projectRepository, MilestoneRepository milestoneRepository) {
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
    }


    /**
     * Mapping for a put request to add a milestone.
     * The method first parses a date string that is passed as a request parameter.
     * The parser converts it to the standard LocalDate format.
     *
     * The project is then grabbed from the repository by its ID.
     * If the project can't be found, it throws an EntityNotFoundException
     *
     * The Milestone is then created with the parameters passed, and saved to the milestone repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param projectId id of project to add milestone to.
     * @param name      Name of milestone.
     * @param end       date of the end of the milestone.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addMilestone")
    public ResponseEntity<Object> addMilestone(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "milestoneName") String name,
            @RequestParam(value = "milestoneEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfOccasion") int typeOfOccasion
    ) {
        try {
            logger.info("PUT /addMilestone");
            LocalDate milestoneEnd = LocalDate.parse(end);
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            if (!regexPatterns.getTitleRegex().matcher(name).matches()) {
                String returnMessage = "Name does not match required pattern";
                logger.warn("PUT /addMilestone: {}", returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }

            Milestone milestone = new Milestone(project, name, milestoneEnd, typeOfOccasion);
            milestoneRepository.save(milestone);
            logger.info("PUT /addMilestone: Success");
            return new ResponseEntity<>(milestone, HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.warn("PUT /addMilestone: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException err) {
            logger.warn("PUT /addMilestone: {}", err.getMessage());
            return new ResponseEntity<>("Could not parse date(s)", HttpStatus.BAD_REQUEST);
        } catch (DateTimeException err) {
            logger.warn("PUT /addMilestone: {}", err.getMessage());
            return new ResponseEntity<>("End date must occur during project", HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("PUT /addMilestone: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Mapping for a post request to edit a milestone.
     * The method first gets the milestone from the repository. If the milestone cannot be retrieved, it throws an EntityNotFound exception.
     *
     * The method then parses a date string that is passed as a request parameter.
     * The parser converts it to the standard LocalDateTime format.
     *
     * The Milestone is then edited with the parameters passed, and saved to the milestone repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param milestoneId the ID of the milestone being edited.
     * @param name the new name of the milestone.
     * @param date the new date of the milestone.
     * @param typeOfOccasion the new type of the milestone.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/editMilestone")
    public ResponseEntity<Object> editMilestone(
            @RequestParam(value = "milestoneId") String milestoneId,
            @RequestParam(value = "milestoneName") String name,
            @RequestParam(value = "milestoneDate") String date,
            @RequestParam(defaultValue = "1", value = "typeOfMilestone") int typeOfOccasion
    ) {
        try {
            logger.info("PUT /editMilestone");
            Milestone milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new EntityNotFoundException(
                    "Milestone with id " + milestoneId + " was not found"
            ));

            LocalDate milestoneDate = LocalDate.parse(date);

            milestone.setName(name);
            milestone.setEndDate(milestoneDate);
            milestone.setType(typeOfOccasion);

            milestoneRepository.save(milestone);

            logger.info("PUT /editMilestone: Success");
            return new ResponseEntity<>(milestone.getId(),HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.warn("PUT /editMilestone: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(DateTimeParseException err) {
            logger.warn("PUT /editMilestone: {}", err.getMessage());
            return new ResponseEntity<>("Could not parse date(s)", HttpStatus.BAD_REQUEST);
        } catch (DateTimeException err) {
            logger.warn("PUT /editMilestone: {}", err.getMessage());
            return new ResponseEntity<>("End date must occur during project", HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("PUT /editMilestone: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    /**
     * Gets the list of milestones in a project and returns it.
     *
     * @param projectId The projectId to get the milestones from this project
     * @return A ResponseEntity with the milestones or an error
     */
    @GetMapping("/getMilestonesList")
    public ResponseEntity<Object> getMilestonesList(
            @RequestParam(value="projectId") Long projectId
    ){
        try {
            logger.info("GET /getMilestoneList");
            List<Milestone> milestoneList = milestoneRepository.findAllByProjectIdOrderByEndDate(projectId);
            milestoneList.sort(Comparator.comparing(Milestone::getEndDate));
            return new ResponseEntity<>(milestoneList, HttpStatus.OK);
        } catch(Exception err){
            logger.error("GET /getMilestoneList: {}", err.getMessage() );
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns a single milestone from the id that was given
     *
     * @param milestoneId The milestone id
     * @return a single milestone
     */
    @GetMapping("/getMilestone")
    public ResponseEntity<Object> getMilestone(
            @RequestParam(value="milestoneId") String milestoneId
    ){
        try {
            logger.info("GET /getMilestone");
            Milestone milestone = milestoneRepository.findById(milestoneId).orElseThrow();
            return new ResponseEntity<>(milestone, HttpStatus.OK);
        } catch(NoSuchElementException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception err){
            logger.error("GET /getMilestone: {}", err.getMessage() );
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Deletes the milestone by the given milestoneId. Teacher or Admin rights are required.
     *
     * @param milestoneId The id of the milestone to be deleted
     * @return The HTTP response conaining the deletion status (OK, NOT_FOUND or INTERNAL SERVER ERROR)
     */
    @DeleteMapping("/deleteMilestone")
    public ResponseEntity<Object> deleteMilestone(
            @RequestParam(value = "milestoneId") String milestoneId
    ) {
        try{
            logger.info("DELETE: /deleteMilestone");
            Milestone milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new EntityNotFoundException(
                    "Milestone with id " + milestoneId + " was not found"
            ));
            milestoneRepository.delete(milestone);
            logger.info("DELETE: /deleteMilestone: Success");
            return new ResponseEntity<>(HttpStatus.OK);

        } catch(EntityNotFoundException err) {
            logger.warn("DELETE: /deleteMilestone: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception err){
            logger.error("DELETE: /deleteMilestone: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
