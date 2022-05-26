package nz.ac.canterbury.seng302.portfolio.controller;

import com.google.type.DateTime;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CalendarController {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final EventRepository eventRepository;
    private final DeadlineRepository deadlineRepository;
    private final MilestoneRepository milestoneRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int tooltipLength = 20;

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    public CalendarController(ProjectRepository projectRepository, SprintRepository sprintRepository, EventRepository eventRepository, DeadlineRepository deadlineRepository, MilestoneRepository milestoneRepository) {
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.eventRepository = eventRepository;
        this.deadlineRepository = deadlineRepository;
        this.milestoneRepository = milestoneRepository;
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
            List<UserRole> roles = user.getRolesList();
            if (roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
                model.addObject("userCanEdit", true);
            } else {
                model.addObject("userCanEdit", false);
            }
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
            List<HashMap<String, Object>> sprintsToSend = new ArrayList<>();

            for (Sprint sprint:sprints)  {
                if(sprint.getStartDate().equals(sprintStartDate)
                        || sprint.getStartDate().isAfter(sprintStartDate) && sprint.getStartDate().isBefore(sprintEndDate)
                        || sprint.getEndDate().equals(sprintEndDate)
                        || sprint.getEndDate().isBefore(sprintEndDate)
                        || sprint.getStartDate().isBefore(sprintStartDate) && sprint.getEndDate().isAfter(sprintEndDate)) {
                    HashMap<String, Object> jsonedSprint = new HashMap<>();
                    jsonedSprint.put("title", sprint.getName());
                    jsonedSprint.put("id", sprint.getId());
                    jsonedSprint.put("start", (LocalDateTime.from(sprint.getStartDate().atStartOfDay())).toString());
                    jsonedSprint.put("end", (LocalDateTime.from(sprint.getEndDate().atStartOfDay().plusHours(24))).toString());
                    jsonedSprint.put("description", sprint.getDescription());
                    jsonedSprint.put("backgroundColor", sprint.getColour());
                    jsonedSprint.put("defaultColor", sprint.getColour());
                    jsonedSprint.put("allDay", true);
                    jsonedSprint.put("isSprint", true);
                    jsonedSprint.put("selected", false);
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
            jsonedProject.put("end", project.getEndDate().plusDays(1).toString());
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
     * Returns the events as in a json format, only finds the events by date
     */
    @GetMapping("getEventsAsFeed")
    public ResponseEntity<Object> getEventsAsFeed(
            @RequestParam(value="projectId") long projectId){
        try{
            logger.info("GET REQUEST /getEventsAsFeed");
            Project project = projectRepository.getProjectById(projectId);

            List<HashMap<String, String>> eventsList = new ArrayList<>();
            HashMap<LocalDate, Integer> eventsCount = new HashMap<>();
            HashMap<LocalDate, String> eventsNames = new HashMap<>();
            List<Event> allEvents = eventRepository.findAllByProjectIdOrderByStartDate(projectId);

            for (Event event : allEvents)  {
                List<LocalDate> dates = new ArrayList<>();
                LocalDateTime current = event.getStartDate();
                while (current.isBefore(LocalDateTime.of(event.getEndDate(), event.getEndTime()))) {
                    dates.add(current.toLocalDate());
                    current = current.plusDays(1);
                }

                for (LocalDate date: dates) {
                    Integer countByDate = eventsCount.get(date);
                    String namesByDate = eventsNames.get(date);
                    String lineEnd = "\r";
                    if (event.getName().length() > tooltipLength) {
                        lineEnd = "...\r";
                    }
                    if (countByDate == null) {
                        eventsCount.put(date, 1); //add date to map as key
                        eventsNames.put(date,event.getName().substring(0, Math.min(event.getName().length(), tooltipLength)) + lineEnd);
                    } else {
                        countByDate++;
                        namesByDate += (event.getName().substring(0, Math.min(event.getName().length(), tooltipLength))+ lineEnd);
                        eventsNames.replace(date, namesByDate);
                        eventsCount.replace(date, countByDate);
                    }
                }
            }

            for (Map.Entry<LocalDate, Integer> entry : eventsCount.entrySet()) {
                HashMap<String, String> jsonedEvent = new HashMap<>();
                jsonedEvent.put("title", String.valueOf(entry.getValue()));
                jsonedEvent.put("occasionTitles",eventsNames.get(entry.getKey()));
                jsonedEvent.put("classNames", "eventCalendar");
                jsonedEvent.put("content", "");
                jsonedEvent.put("start", entry.getKey().toString());
                jsonedEvent.put("end", entry.getKey().toString());
                eventsList.add(jsonedEvent);
            }

            return new ResponseEntity<>(eventsList, HttpStatus.OK);
        } catch(DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err){
            logger.error("GET REQUEST /getEventsAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Returns the deadlines in a json format, with the date of the deadline mapped to the number of deadline occurring on that date.
     */
    @GetMapping("getDeadlinesAsFeed")
    public ResponseEntity<Object> getDeadlinesAsFeed(
            @RequestParam(value="projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getDeadlinesAsFeed");
            Project project = projectRepository.getProjectById(projectId);

            List<HashMap<String, String>> deadlinesList = new ArrayList<>();
            HashMap<LocalDate, Integer> deadlinesCount = new HashMap<>();
            HashMap<LocalDate, String> deadlinesNames = new HashMap<>();
            List<Deadline> allDeadlines = deadlineRepository.findAllByProjectIdOrderByEndDate(projectId);

            for (Deadline deadline : allDeadlines) { //iterates over all milestones in repo, and counts the
                Integer countByDate = deadlinesCount.get(deadline.getEndDate());
                String namesByDate = deadlinesNames.get(deadline.getEndDate());
                String lineEnd = "\r";
                if (deadline.getName().length() > tooltipLength) {
                    lineEnd = "...\r";
                }
                if (countByDate == null) {
                    deadlinesCount.put(deadline.getEndDate(), 1); //add date to map as key
                    deadlinesNames.put(deadline.getEndDate(), deadline.getName().substring(0, Math.min(deadline.getName().length(), tooltipLength)) + lineEnd);
                }else {

                    countByDate++;
                    deadlinesCount.replace(deadline.getEndDate(), countByDate);
                    namesByDate += (deadline.getName().substring(0, Math.min(deadline.getName().length(), tooltipLength)) + lineEnd);
                    deadlinesNames.replace(deadline.getEndDate(), namesByDate);
                }
            }

            for (Map.Entry<LocalDate, Integer> entry : deadlinesCount.entrySet()) {
                HashMap<String, String> jsonedDeadline = new HashMap<>();
                jsonedDeadline.put("title", String.valueOf(entry.getValue()));
                jsonedDeadline.put("occasionTitles", deadlinesNames.get(entry.getKey()));
                jsonedDeadline.put("classNames", "deadlineCalendar");
                jsonedDeadline.put("content", "");
                jsonedDeadline.put("start", entry.getKey().toString());
                jsonedDeadline.put("end", entry.getKey().toString());
                deadlinesList.add(jsonedDeadline);
            }

            return new ResponseEntity<>(deadlinesList, HttpStatus.OK);
        } catch(DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err){
            logger.error("GET REQUEST /getDeadlinesAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Returns the milestones in a json format, with the date of the milestone mapped to the number of milestones occurring on that date.
     */
    @GetMapping("getMilestonesAsFeed")
    public ResponseEntity<Object> getMilestonesAsFeed(
            @RequestParam(value="projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getMilestonesAsFeed");
            Project project = projectRepository.getProjectById(projectId);

            List<HashMap<String, String>> milestonesList = new ArrayList<>();
            HashMap<LocalDate, Integer> milestonesCount = new HashMap<>();
            HashMap<LocalDate, String> milestonesNames = new HashMap<>();
            List<Milestone> allMilestones = milestoneRepository.findAllByProjectIdOrderByEndDate(projectId);

            for (Milestone milestone : allMilestones) { //iterates over all milestones in repo, and counts the
                Integer countByDate = milestonesCount.get(milestone.getEndDate());
                String namesByDate = milestonesNames.get(milestone.getEndDate());
                String lineEnd = "\r";
                if (milestone.getName().length() > tooltipLength) {
                    lineEnd = "...\r";
                }
                if (countByDate == null) {
                    milestonesCount.put(milestone.getEndDate(), 1); //add date to map as key
                    milestonesNames.put(milestone.getEndDate(), milestone.getName().substring(0,Math.min(milestone.getName().length(), tooltipLength))+ lineEnd);
                }else {
                    countByDate++;
                    milestonesCount.replace(milestone.getEndDate(), countByDate);
                    namesByDate += (milestone.getName().substring(0,Math.min(milestone.getName().length(), tooltipLength)) + lineEnd);
                    milestonesNames.replace(milestone.getEndDate(), namesByDate);
                }
            }


            for (Map.Entry<LocalDate, Integer> entry : milestonesCount.entrySet()) {
                HashMap<String, String> jsonedMilestone = new HashMap<>();
                jsonedMilestone.put("title", String.valueOf(entry.getValue()));
                jsonedMilestone.put("occasionTitles", milestonesNames.get(entry.getKey()));
                jsonedMilestone.put("classNames", "milestoneCalendar");
                jsonedMilestone.put("content", "");
                jsonedMilestone.put("start", entry.getKey().toString());
                jsonedMilestone.put("end", entry.getKey().toString());
                milestonesList.add(jsonedMilestone);
            }

            return new ResponseEntity<>(milestonesList, HttpStatus.OK);
        } catch(DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err){
            logger.error("GET REQUEST /getMilestonesAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
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

