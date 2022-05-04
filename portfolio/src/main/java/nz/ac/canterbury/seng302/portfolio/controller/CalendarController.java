package nz.ac.canterbury.seng302.portfolio.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
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
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
     * @param request request
     * @return the calendar view
     */
    @GetMapping("/calendar")
    public ModelAndView getCalendar(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "projectId") Long projectId,
            HttpServletRequest request
            ) {
        try{
            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + projectId + " was not found"
            ));

            ModelAndView model = new ModelAndView("monthlyCalendar");
            model.addObject("project", project);
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
            String ip = request.getLocalAddr();
            String url = "http://" + ip + ":9001/" + user.getProfileImagePath();
            model.addObject("profileImageUrl", url);
            model.addObject("username", user.getUsername());
            return model;

        } catch (EntityNotFoundException err){
            logger.error("GET REQUEST /calendar", err);
            return new ModelAndView("errorPage").addObject("errorMessage", err.getMessage());
        }



    }


    /**
     * Gets the project sprints and returns them in a response entity
     * @param projectId id of the project that the sprints are contained in.
     * @return Response entity with sprints in them, or the error.
     */
    @GetMapping("/getProjectSprints")
    public ResponseEntity<Object> getProjectSprints(@RequestParam(value = "projectId") Long projectId){
        try{
            logger.info("GET REQUEST /getProjectSprints");
            List<Sprint> sprints = sprintRepository.findAllByProjectId(projectId);
            return new ResponseEntity<>(sprints, HttpStatus.OK);
        } catch (Exception err){
            logger.error("GET REQUEST /getProjectSprints", err);
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
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

            startDate = startDate.substring(0, startDate.length()-6);
            endDate = endDate.substring(0, endDate.length()-6);
            //TODO figure out this timezone stuff, at the moment its being removed and then a constant of 12 is being added
            LocalDate sprintStartDate = LocalDate.from(LocalDateTime.parse(startDate));
            LocalDate sprintEndDate = LocalDate.from(LocalDateTime.parse(endDate));


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
                    jsonedSprint.put("start", (LocalDateTime.from(sprint.getStartDate().atStartOfDay())).toString());
                    jsonedSprint.put("end", (LocalDateTime.from(sprint.getEndDate().atStartOfDay())).toString());
                    jsonedSprint.put("backgroundColor", sprint.getColour());
                    sprintsToSend.add(jsonedSprint);
                }
            }

            return new ResponseEntity<>(sprintsToSend, HttpStatus.OK);
        } catch(DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err){
            logger.error("GET REQUEST /getProjectSprints", err);
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Gets project in a json feed format
     * @param projectId project to get
     * @param startDate start date to look for
     * @param endDate end date to look for
     * @return ResponseEntity with status, and List of hashmaps.
     */
    @GetMapping("/getProjectAsFeed")
    public ResponseEntity<Object> getProjectWithDates(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "start") String startDate,
            @RequestParam(value = "end") String endDate){
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
        } catch(DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch(EntityNotFoundException err) {
            logger.warn(err.getMessage());
            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception err){
            logger.error("GET REQUEST /getProjectSprints", err);
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }

    }





    /**
     * Gets the project detils
     * @param projectId project to get
     * @return response entity with project, or error message
     */
    @GetMapping("/getProjectDetails")
    public ResponseEntity<Object> getProject(
            @RequestParam(value="projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getProject");

            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));
            return new ResponseEntity<>(project, HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.error("GET REQUEST /getProject", err);
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

