package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.sprints.SprintRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class CalendarController {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    public CalendarController(ProjectRepository projectRepository, SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
    }


    /**
     * Get mapping for /calendar. Returns the calendar view.
     * @param principal principal
     * @param projectId id of the project that the calendar will display
     * @return the calendar view
     */
    @GetMapping("/calendar")
    public ModelAndView getCalendar(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "projectId") Long projectId
            ) {
        try{
            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + projectId + " was not found"
            ));

            ModelAndView model = new ModelAndView("monthlyCalendar");
            model.addObject("project", project);
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
            model.addObject("user", user);
            return model;

        } catch (EntityNotFoundException err){
            logger.error("GET REQUEST /calendar", err);
            return new ModelAndView("errorPage").addObject("errorMessage", err.getMessage());
        }



    }



    /**
     * Returns the sprints as in a json format, only finds the sprints that are within the start and end dates
     * @param projectId the project to look for the sprints in
     * @param startDate start date to look for
     * @param endDate end date to look for
     * @return ResponseEntity with status, and List of hashmaps.
     */
    @GetMapping("/getProjectSprintsWithDatesAsFeed")
    public ResponseEntity<Object> getProjectSprintsWithDates(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "start") String startDate,
            @RequestParam(value = "end") String endDate){
        try{
            logger.info("GET REQUEST /getProjectSprintsWithDatesAsFeed");


            // It receives the startDate and endDate in a ZonedDateTime format.
            ZonedDateTime startDateLocalDateTime = ZonedDateTime.parse(startDate);
            ZonedDateTime endDateDateLocalDateTime = ZonedDateTime.parse(endDate);

            // To check against the sprints we need to convert from ZonedDateTime to LocalDate
            LocalDate sprintStartDate = LocalDate.from(startDateLocalDateTime);
            LocalDate sprintEndDate = LocalDate.from(endDateDateLocalDateTime);


            List<Sprint> sprints = sprintRepository.findAllByProjectId(projectId);
            List<HashMap<String, String>> sprintsToSend = new ArrayList<>();

            for (Sprint sprint:sprints)  {
                if(sprint.getStartDate().equals(sprintStartDate)
                        || sprint.getStartDate().isAfter(sprintStartDate) && sprint.getStartDate().isBefore(sprintEndDate)
                        || sprint.getEndDate().equals(sprintEndDate)
                        || sprint.getEndDate().isBefore(sprintEndDate)
                        || sprint.getStartDate().isBefore(sprintStartDate) && sprint.getEndDate().isAfter(sprintEndDate)) {
                    HashMap<String, String> jsonedSprint = new HashMap<>();
                    jsonedSprint.put("title", sprint.getName());
                    jsonedSprint.put("id", sprint.getId().toString());
                    jsonedSprint.put("start", (LocalDateTime.from(sprint.getStartDate().atStartOfDay())).toString());
                    jsonedSprint.put("end", (LocalDateTime.from(sprint.getEndDate().atStartOfDay().plusHours(24))).toString());
                    jsonedSprint.put("description", sprint.getDescription());
                    jsonedSprint.put("backgroundColor", sprint.getColour());
                    jsonedSprint.put("allDay", "true");
                    sprintsToSend.add(jsonedSprint);
                }
            }

            return new ResponseEntity<>(sprintsToSend, HttpStatus.OK);
        } catch(DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err){
            logger.error("GET REQUEST /getProjectSprintsWithDatesAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Gets project in a json feed format
     * @param projectId project to get
     * @return ResponseEntity with status, and List of hashmaps.
     */
    @GetMapping("/getProjectAsFeed")
    public ResponseEntity<Object> getProject(
            @RequestParam(value = "projectId") Long projectId){
        try{
            logger.info("GET REQUEST /getProjectAsFeed");


            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            List<HashMap<String, String>> projectToSend = new ArrayList<>();

            HashMap<String, String> jsonedProject = new HashMap<>();
            jsonedProject.put("title", project.getName());
            jsonedProject.put("start", project.getStartDate().toString());
            jsonedProject.put("end", project.getEndDate().toString());
            jsonedProject.put("backgroundColor", "grey");

            projectToSend.add(jsonedProject);

            return new ResponseEntity<>(projectToSend, HttpStatus.OK);

        } catch(EntityNotFoundException err) {
            logger.warn(err.getMessage());
            return new ResponseEntity<>(err.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception err){
            logger.error("GET REQUEST /getProjectAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    /**
     * Gets the project details
     * @param projectId project to get
     * @return response entity with project, or error message
     */
    @GetMapping("/getProjectDetails")
    public ResponseEntity<Object> getProject(
            @RequestParam(value="projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getProjectDetails");

            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));
            return new ResponseEntity<>(project, HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.error("GET REQUEST /getProjectDetails", err);
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }



    /**
     * For testing
     * @param service service
     */
    public void setUserAccountsClientService(UserAccountsClientService service) {
        this.userAccountsClientService = service;
    }

}

