package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.naming.InvalidNameException;
import javax.persistence.EntityNotFoundException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
public class DeadlineController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

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
     * @param principal The AuthState of the user making the request, for authentication
     * @param projectId id of project to add deadline to.
     * @param name      Name of milestone.
     * @param end       end of the deadline
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addDeadline")
    public ResponseEntity<String> addDeadline(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "deadlineName") String name,
            @RequestParam(value = "deadlineEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfOccasion") int typeOfOccasion
    ) {
        logger.info("PUT /addDeadline");
        UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        // Checks what role the user has and if it's not a teacher or a course admin it returns a forbidden response
        List<UserRole> roles = userResponse.getRolesList();
        if (!roles.contains(UserRole.TEACHER) && !roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            logger.info("PUT /addDeadline: Unauthorised User");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            if (name == null) {
                Long count = deadlineRepository.countDeadlineByProjectId(projectId);
                name = "Deadline " + (count + 1);
            } else if (name.length() > 50) {
                throw new InvalidNameException("The name of a deadline cannot be more than 50 characters");
            }
            //  Get the deadline end dateTime.
            // end returns a string in the format "1986-01-28T11:38:00.01"
            // DateTimeFormatter.ISO_DATE_TIME helps parse that string by declaring its format.
            LocalDateTime deadlineEnd;
            if (end == null) {  // if the date is empty then set it as the start of the project or today's date
                if (LocalDate.now().isAfter(project.getStartDate())) {
                    deadlineEnd = LocalDateTime.now();
                } else {
                    deadlineEnd = project.getStartDateAsLocalDateTime();
                }
            } else {
                deadlineEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);
            }
            //Check to see if the dates are within the correct range
            if (deadlineEnd.isAfter(project.getEndDateAsLocalDateTime()) || deadlineEnd.isBefore(project.getStartDateAsLocalDateTime())){
                String returnMessage = "Date(s) exist outside of project dates";
                logger.warn("PUT /addDeadline: {}", returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }
            //Check if the type of occasion is valid
            if (typeOfOccasion < 1) {
                String returnMessage = "Invalid type of occasion";
                logger.warn("PUT /addDeadline: {}", returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }

            Deadline deadline = new Deadline(project, name, deadlineEnd.toLocalDate(), deadlineEnd.toLocalTime(), typeOfOccasion);
            deadlineRepository.save(deadline);

            logger.info("PUT /addDeadline: Success");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.warn("PUT /addDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (InvalidNameException | IllegalArgumentException | DateTimeException err) {
            logger.warn("PUT /addDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.warn("PUT /addDeadline: {}", err.getMessage());
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
     * @param principal The AuthState of the user making the request, for authentication
     * @param deadlineId the ID of the deadline being edited.
     * @param projectId id of project to add deadline to.
     * @param name the new name of the deadline.
     * @param dateEnd the new date of the deadline.
     * @param timeEnd the new time of the deadline
     * @param typeOfOccasion the new type of the deadline.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/editDeadline")
    public ResponseEntity editDeadline(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "deadlineId") UUID deadlineId,
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "deadlineName") String name,
            @RequestParam(value = "deadlineDate") String dateEnd,
            @RequestParam(value = "deadlineTime") String timeEnd,
            @RequestParam(value = "typeOfOccasion") Integer typeOfOccasion
    ) {
        logger.info("PUT /editDeadline");
        UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        // Checks what role the user has and if it's not a teacher or a course admin it returns a forbidden response
        List<UserRole> roles = userResponse.getRolesList();
        if (!roles.contains(UserRole.TEACHER) && !roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            logger.info("PUT /editDeadline: Unauthorised User");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow(() -> new EntityNotFoundException(
                    "Deadline with id " + deadlineId + " was not found"
            ));

            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            // if no name is given then keep the name it has already
            if (name != null) {
                if (name.length() > 50) {
                    throw new InvalidNameException("The name of a deadline cannot be more than 50 characters");
                } else {
                    deadline.setName(name);
                }
            }

            LocalDate deadlineEndDate;
            LocalTime deadlineEndTime;
            if (dateEnd != null) {  // if the date is empty then keep it as it is
                deadlineEndDate = LocalDate.parse(dateEnd);
                if (deadlineEndDate.isAfter(project.getEndDate()) || deadlineEndDate.isBefore(project.getStartDate())){
                    throw new DateTimeException("The deadline date cannot be outside of the project");
                }
                deadline.setEndDate(deadlineEndDate);
            }

            if (timeEnd != null){ // if the time is empty then keep it as it is
                deadlineEndTime = LocalTime.parse(timeEnd);
                deadline.setEndTime(deadlineEndTime);
            }
            if (typeOfOccasion != null) {
                if (typeOfOccasion < 1){
                    throw new IllegalArgumentException("The type of the deadline is not a valid");
                }
                deadline.setType(typeOfOccasion);
            }

            deadlineRepository.save(deadline);
            logger.info("PUT /deleteDeadline: Success");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.warn("PUT /editDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (InvalidNameException | IllegalArgumentException | DateTimeException err) {
            logger.warn("PUT /editDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.warn("PUT /editDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mapping for deleting an existing deadline.
     * The method attempts to get the deadline from the repository and if it cannot it will throw an EntityNotFoundException
     *
     * Otherwise it will delete the deadline from the repository
     *
     * @param principal The AuthState of the user making the request, for authentication
     * @param deadlineId The UUID of the deadline to be deleted
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/deleteDeadline")
    public ResponseEntity deleteDeadline(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "deadlineId") UUID deadlineId) {
        logger.info("PUT /deleteDeadline");
        UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        // Checks what role the user has and if it's not a teacher or a course admin it returns a forbidden response
        List<UserRole> roles = userResponse.getRolesList();
        if (!roles.contains(UserRole.TEACHER) && !roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            logger.info("PUT /deleteDeadline: Unauthorised User");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow(() -> new EntityNotFoundException(
                    "Deadline with id " + deadlineId + " was not found"
            ));
            deadlineRepository.delete(deadline);
            logger.info("PUT /deleteDeadline: Success");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.warn("PUT /deleteDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception err) {
            logger.warn("PUT /deleteDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets the list of deadlines in a project and returns it.
     * @param projectId The projectId to get the deadlines from this project
     * @return A ResponseEntity with the deadlines or an error
     */
    @GetMapping("/getDeadlinesList")
    public ResponseEntity<Object> getDeadlinesList(
            @RequestParam(value="projectId") Long projectId
    ){
        try {
            logger.info("GET /getDeadlinesList");
            List<Deadline> deadlineList = deadlineRepository.findAllByProjectId(projectId);
            deadlineList.sort(Comparator.comparing(Deadline::getDateTime));
            return new ResponseEntity<>(deadlineList, HttpStatus.OK);
        } catch(Exception err){
            logger.error("GET /getDeadlineList: {}", err.getMessage() );
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns a single deadline from the id that was given
     * @param deadlineId The deadline id
     * @return a single deadline
     */
    @GetMapping("/getDeadline")
    public ResponseEntity<Object> getDeadline(
            @RequestParam(value="deadlineId") UUID deadlineId
    ){
        try {
            logger.info("GET /getDeadline");
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow();
            return new ResponseEntity<>(deadline, HttpStatus.OK);
        } catch(NoSuchElementException err) {
            logger.error("GET /getDeadline: {}", err.getMessage() );
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception err){
            logger.error("GET /getDeadline: {}", err.getMessage() );
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Used to set a userAccountClientService if not using the autowired one. Useful for testing and mocking
     * @param service The userAccountClientService to be used
     */
    public void setUserAccountsClientService(UserAccountsClientService service) { this.userAccountsClientService = service;}

}
