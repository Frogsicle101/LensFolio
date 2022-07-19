package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.RegexPatterns;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventHelper;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneHelper;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.CheckDateService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.InvalidNameException;
import javax.persistence.EntityNotFoundException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@Controller
public class PortfolioController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    @Autowired
    private final SprintRepository sprintRepository;

    @Autowired
    private final ProjectRepository projectRepository;

    @Autowired
    private final EventRepository eventRepository;

    @Autowired
    private final DeadlineRepository deadlineRepository;

    @Autowired
    private final MilestoneRepository milestoneRepository;

    //Selectors for the error/info/success boxes.
    private static final String ERROR_MESSAGE = "errorMessage";


    private final CheckDateService checkDateService = new CheckDateService();

    RegexPatterns regexPatterns = new RegexPatterns();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // For testing
    private static final boolean INCLUDE_TEST_VALUES = true;


    /**
     * Constructor for PortfolioController
     *
     * @param sprintRepository    repository
     * @param projectRepository   repository
     * @param milestoneRepository repository
     */
    public PortfolioController(SprintRepository sprintRepository,
                               ProjectRepository projectRepository,
                               EventRepository eventRepository,
                               MilestoneRepository milestoneRepository,
                               DeadlineRepository deadlineRepository) throws InvalidNameException {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
        this.milestoneRepository = milestoneRepository;
        this.deadlineRepository = deadlineRepository;

        //Below are only for testing purposes.
        if (INCLUDE_TEST_VALUES) {
            Project defaultProject = projectRepository.save(new Project("Project Seng302",
                    LocalDate.parse("2022-02-25"),
                    LocalDate.parse("2022-09-30"),
                    "SENG302 is all about putting all that you have learnt in" +
                            " other courses into a systematic development process to" +
                            " create software as a team."));
            createDefaultEvents(defaultProject);
            createDefaultSprints(defaultProject);
            createDefaultMilestones(defaultProject);
            createDefaultDeadlines(defaultProject);
        } else {
            projectRepository.save(new Project("Default Project"));
        }

    }


    /**
     * Get mapping for /Portfolio
     *
     * @param principal - The Authentication of the user making the request, for authentication
     * @param projectId Id of the project to display
     * @return returns the portfolio view, or error-page
     */
    @GetMapping("/portfolio")
    public ModelAndView getPortfolio(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "projectId") long projectId
    ) {
        try {

            logger.info("GET REQUEST /portfolio");

            // Get user from server
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);


            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            ModelAndView modelAndView = new ModelAndView("portfolio");

            // Checks what role the user has. Adds boolean object to the view so that displays can be changed on the frontend.
            List<UserRole> roles = user.getRolesList();
            if (roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
                modelAndView.addObject("userCanEdit", true);
            } else {
                modelAndView.addObject("userCanEdit", false);
            }

            List<Event> eventList = EventHelper.setEventColours(project.getId(), eventRepository, sprintRepository);
            List<Milestone> milestoneList = MilestoneHelper.setMilestoneColours(project.getId(), milestoneRepository, sprintRepository);

            int nextMilestoneNumber = milestoneRepository.countMilestoneByProjectId(projectId).intValue() + 1;
            LocalDate defaultOccasionDate = project.getStartDate(); // Today is in a sprint, the start of th project otherwise
            if (checkDateService.dateIsInSprint(LocalDate.now(), project, sprintRepository)) {
                defaultOccasionDate = LocalDate.now();
            }

            modelAndView.addObject("project", project);
            modelAndView.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));
            modelAndView.addObject("events", eventList);
            modelAndView.addObject("milestones", milestoneList);
            modelAndView.addObject("nextMilestoneNumber", nextMilestoneNumber);
            modelAndView.addObject("eventNameLengthRestriction", Milestone.getNameLengthRestriction());
            modelAndView.addObject("defaultOccasionDate", defaultOccasionDate);
            modelAndView.addObject("user", user);
            modelAndView.addObject("projectId", projectId);
            modelAndView.addObject("titleRegex", regexPatterns.getTitleRegex().toString());
            modelAndView.addObject("descriptionRegex", regexPatterns.getDescriptionRegex().toString());

            return modelAndView;

        } catch (EntityNotFoundException err) {
            logger.error("GET REQUEST /portfolio", err);
            return new ModelAndView("errorPage").addObject(ERROR_MESSAGE, err.getMessage());
        } catch (Exception err) {
            logger.error("GET REQUEST /portfolio", err);
            return new ModelAndView("errorPage").addObject(ERROR_MESSAGE, err);
        }
    }


    /**
     * Request mapping for /editProject
     *
     * @param principal - The Authentication of the user making the request, for authentication
     * @param projectId The project to edit
     * @return Returns the project edit page or the error page
     */
    @RequestMapping("/editProject")
    public ModelAndView edit(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "projectId") Long projectId
    ) {
        try {
            logger.info("GET REQUEST /editProject");

            // Get user from server
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);

            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + projectId + "was not found"
            ));

            // The view we are going to return.
            ModelAndView modelAndView = new ModelAndView("projectEdit");

            // Adds the project object to the view for use.
            modelAndView.addObject("project", project);

            // Adds the username and profile photo to the view for use.
            modelAndView.addObject("user", user);

            return modelAndView;

        } catch (EntityNotFoundException err) {
            logger.error("GET REQUEST /editProject", err);
            return new ModelAndView("errorPage").addObject(ERROR_MESSAGE, err);
        } catch (Exception err) {
            logger.error("GET REQUEST /editProject", err);
            return new ModelAndView("errorPage");
        }
    }


    /**
     * Postmapping for /projectEdit, this is called when user submits there project changes.
     *
     * @param editInfo A DTO of project from the inputs on the edit page.
     * @return Returns to the portfolio page.
     */
    @PostMapping("/projectEdit")
    public ResponseEntity<Object> editDetails(
            @ModelAttribute(name = "editProjectForm") ProjectRequest editInfo
    ) {
        try {
            logger.info("POST REQUEST /projectEdit");

            ResponseEntity<Object> parsedProjectRequest = checkProjectRequest(editInfo);
            if (parsedProjectRequest.getStatusCode() != HttpStatus.OK) {
                logger.error("/projectEdit error: {}", parsedProjectRequest.getBody());
                return parsedProjectRequest;
            }

            LocalDate projectStart = LocalDate.parse(editInfo.getProjectStartDate());
            LocalDate projectEnd = LocalDate.parse(editInfo.getProjectEndDate());


            Project project = projectRepository.findById(Long.parseLong(editInfo.getProjectId())).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + editInfo.getProjectId() + "was not found"
            ));

            if (projectStart.isBefore(project.getStartDate().minusYears(1))) {
                return new ResponseEntity<>("Project cannot start more than a year before its original date", HttpStatus.BAD_REQUEST);
            }

            List<Sprint> sprintListEndDates = sprintRepository.getAllByProjectOrderByEndDateDesc(project);
            List<Sprint> sprintListStartDates = sprintRepository.getAllByProjectOrderByStartDateAsc(project);
            if (!sprintListEndDates.isEmpty()) {
                Sprint sprint = sprintListEndDates.get(0);
                if (sprint.getEndDate().isAfter(projectEnd)) {
                    String errorMessage = "Could not change project dates.  New project end date of " + projectEnd + " is before the sprint: " + sprint.getName() + " ends: " + sprint.getEndDate().toString();
                    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
                }
                sprint = sprintListStartDates.get(0);
                if (sprint.getStartDate().isBefore(projectStart)) {
                    String errorMessage = "Could not change project dates. New project start date of: " + projectStart + " is after the sprint: " + sprint.getName() + " starts: " + sprint.getStartDate().toString();
                    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
                }
            }

            //Updates the project's details
            project.setName(editInfo.getProjectName());
            project.setStartDate(projectStart);
            project.setEndDate(projectEnd);
            project.setDescription(editInfo.getProjectDescription());
            projectRepository.save(project);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.error("POST REQUEST /projectEdit", err);
            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("POST REQUEST /projectEdit", err);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Checks that the project request is valid, contains all the right things, and dates are correct and in order.
     *
     * @param projectRequest the project request DTO
     * @return Response Entity that is either Ok, or not with issues attached.
     */
    private ResponseEntity<Object> checkProjectRequest(ProjectRequest projectRequest) {
        try {
            int projectId = Integer.parseInt(projectRequest.getProjectId());
            String projectName = projectRequest.getProjectName();
            LocalDate projectStartDate = LocalDate.parse(projectRequest.getProjectStartDate());
            LocalDate projectEndDate = LocalDate.parse(projectRequest.getProjectEndDate());
            String projectDescription = projectRequest.getProjectDescription();

            if (!regexPatterns.getTitleRegex().matcher(projectName).matches()) {
                return new ResponseEntity<>("Project Name contains characters outside of a-z 0-9", HttpStatus.BAD_REQUEST);
            }
            if (!regexPatterns.getDescriptionRegex().matcher(projectDescription).matches()) {
                return new ResponseEntity<>("Project description contains illegal characters", HttpStatus.BAD_REQUEST);
            }

            if (projectId < 0) {
                return new ResponseEntity<>("Project id cannot be less than zero", HttpStatus.BAD_REQUEST);
            }

            if (projectEndDate.isBefore(projectStartDate)) {
                return new ResponseEntity<>("End date cannot be before start date", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (NumberFormatException err) {
            return new ResponseEntity<>("Project id is not a parsable integer", HttpStatus.BAD_REQUEST);
        } catch (DateTimeParseException err) {
            return new ResponseEntity<>("Project date(s) are not valid dates", HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get mapping for portfolio/addSprint
     * This is called when user wants to add a sprint.
     *
     * @param projectId Project to add the sprint to.
     * @return a response entity response
     */
    @GetMapping("/portfolio/addSprint")
    public ResponseEntity<Object> addSprint(
            @RequestParam(value = "projectId") Long projectId) {
        try {
            logger.info("GET REQUEST /portfolio/addSprint");

            // Gets the amount of sprints belonging to the project
            int amountOfSprints = sprintRepository.findAllByProjectId(projectId).size() + 1;
            String sprintName = "Sprint " + amountOfSprints;


            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            // Initially startDate is set to the projects start date.
            LocalDate startDate = project.getStartDate();

            //If there are sprints in the repository, startDate is set to the day after the last sprint.
            List<Sprint> sprintList = sprintRepository.getAllByProjectOrderByEndDateDesc(project);
            if (!sprintList.isEmpty()) {
                startDate = sprintList.get(0).getEndDate().plusDays(1);
            }
            //If start date of sprint is after the project end date then send a message to the user informing them
            //that no more sprints can be added.
            if (startDate.isAfter(project.getEndDate())) {
                logger.warn("Could not add anymore sprints, no more room for sprints within project dates");
                return new ResponseEntity<>("No more room to add sprints within project dates!", HttpStatus.BAD_REQUEST);
            } else {
                // Check that if the end date (startDate.plus(3)weeks) is after project end date, then set the end date
                // to be the project end date.
                if (startDate.plusWeeks(3).isAfter(project.getEndDate())) {
                    //Save the new sprint
                    sprintRepository.save(new Sprint(project, sprintName, startDate, project.getEndDate()));
                } else {
                    //Save the new sprint
                    sprintRepository.save(new Sprint(project, sprintName, startDate));
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception err) {
            logger.error("GET REQUEST /portfolio/addSprint", err);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Mapping for /sprintEdit. Looks for a sprint that matches the id
     * and then populates the form.
     *
     * @param principal The authentication state
     * @param sprintId  The sprint id
     * @return Thymleaf template
     */
    @RequestMapping("/sprintEdit")
    public ModelAndView sprintEdit(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "sprintId") String sprintId,
            @RequestParam(value = "projectId") Long projectId,
            RedirectAttributes attributes
    ) {
        try {
            logger.info("GET REQUEST /sprintEdit");
            ModelAndView modelAndView = new ModelAndView("sprintEdit");

            // Get user from server
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);

            Sprint sprint = sprintRepository.findById(String.valueOf(sprintId)).orElseThrow(() -> new EntityNotFoundException(
                    "Sprint with id " + projectId.toString() + " was not found"
            ));

            Project project = projectRepository.getProjectById(projectId);
            HashMap<String, LocalDate> neighbouringDates = checkNeighbourDatesForSprint(project, sprint);

            String textForPreviousSprint;
            String textForNextSprint;

            modelAndView.addObject("previousSprintEnd", neighbouringDates.get("previousSprintEnd"));
            if (neighbouringDates.get("previousSprintEnd").equals(project.getStartDate())) {
                textForPreviousSprint = "No previous sprints, Project starts on " + neighbouringDates.get("previousSprintEnd");
            } else {
                textForPreviousSprint = "Previous sprint ends on " + neighbouringDates.get("previousSprintEnd");
            }
            modelAndView.addObject("textForPrevSprint", textForPreviousSprint);

            modelAndView.addObject("nextSprintStart", neighbouringDates.get("nextSprintStart"));
            if (neighbouringDates.get("nextSprintStart").equals(project.getEndDate())) {
                textForNextSprint = "No next sprint, project ends on  " + neighbouringDates.get("nextSprintStart");
            } else {
                textForNextSprint = "Next sprint starts on " + neighbouringDates.get("nextSprintStart");
            }
            modelAndView.addObject("textForNextSprint", textForNextSprint);

            // Adds the username to the view for use.
            modelAndView.addObject("user", user);

            // Add the sprint to the view for use.
            modelAndView.addObject("sprint", sprint);

            return modelAndView;
        } catch (Exception err) {
            logger.error("GET REQUEST /sprintEdit", err);
            attributes.addFlashAttribute(ERROR_MESSAGE, err);
            return new ModelAndView("redirect:/portfolio?projectId=" + projectId);
        }
    }


    /**
     * Get a list of all the sprints in a project by the project Id.
     *
     * @param projectId - The project that contains the sprints
     * @return A response entity containing the sprints and the HTTP status
     */
    @GetMapping("/getSprintList")
    public ResponseEntity<Object> getSprintList(
            @RequestParam(value = "projectId") Long projectId
    ) {
        List<Sprint> sprintList = sprintRepository.findAllByProjectId(projectId);
        sprintList.sort(Comparator.comparing(Sprint::getStartDate));
        return new ResponseEntity<>(sprintList, HttpStatus.OK);
    }


    /**
     * Helper function that gets the dates that neighbour the sprint that it is given
     *
     * @param project the project the sprint is in
     * @param sprint  the sprint
     * @return a HashMap that contains keys "previousSprintEnd" and "nextSprintStart"
     */
    private HashMap<String, LocalDate> checkNeighbourDatesForSprint(Project project, Sprint sprint) {
        // Gets a list of all sprints that belong to the project and orders them by start date: earliest to latest
        List<Sprint> sprintList = sprintRepository.getAllByProjectOrderByStartDateAsc(project);

        HashMap<String, LocalDate> neighbouringSprintDates = new HashMap<>();

        int indexOfPrevSprint = sprintList.indexOf(sprint);
        int indexOfNextSprint = sprintList.indexOf(sprint);
        // Checks if the selected sprint is not the first on the list
        if (indexOfPrevSprint > 0) {
            indexOfPrevSprint = indexOfPrevSprint - 1;
            // Adds an object to the view that limits the calendar to dates past the previous sprints end.
            neighbouringSprintDates.put("previousSprintEnd", sprintList.get(indexOfPrevSprint).getEndDate().plusDays(1));
        } else {
            // Else adds an object to the view that limits the calendar to project start .
            neighbouringSprintDates.put("previousSprintEnd", project.getStartDate());
        }
        // Checks if the selected sprint is not the last on the list
        if (indexOfNextSprint < sprintList.size() - 1) {
            indexOfNextSprint = indexOfNextSprint + 1;
            // Adds an object to the view that limits the calendar to dates before the next sprints starts.

            neighbouringSprintDates.put("nextSprintStart", sprintList.get(indexOfNextSprint).getStartDate().minusDays(1));

        } else {
            // Else adds an object to the view that limits the calendar to be before the project end.
            neighbouringSprintDates.put("nextSprintStart", project.getEndDate());
        }

        return neighbouringSprintDates;
    }


    /**
     * Takes the request to update the sprint.
     * Tries to update the sprint then redirects user.
     *
     * @param sprintInfo the thymeleaf-created form object
     * @return redirect to portfolio
     */
    @PostMapping("/sprintSubmit")
    public ResponseEntity<Object> updateSprint(
            @ModelAttribute(name = "sprintEditForm") SprintRequest sprintInfo) {

        try {
            logger.info("POST REQUEST /sprintSubmit");

            // Checks that the sprint request is acceptable
            ResponseEntity<Object> checkSprintRequest = checkSprintRequest(sprintInfo);
            if (checkSprintRequest.getStatusCode() != HttpStatus.OK) {
                logger.warn("/sprintSubmit issue with SprintRequest: {}", checkSprintRequest.getBody());
                return checkSprintRequest;
            }

            LocalDate startDate = LocalDate.parse(sprintInfo.getSprintStartDate());
            LocalDate endDate = LocalDate.parse(sprintInfo.getSprintEndDate());


            Sprint sprint = sprintRepository.getSprintById(sprintInfo.getSprintId());
            Project project = sprint.getProject();

            HashMap<String, LocalDate> checkSprintDates = checkNeighbourDatesForSprint(project, sprint);
            LocalDate previousDateLimit = checkSprintDates.get("previousSprintEnd");
            LocalDate nextDateLimit = checkSprintDates.get("nextSprintStart");

            if (startDate.isBefore(previousDateLimit)) {
                return new ResponseEntity<>("Start date is before previous sprints end date / project start date", HttpStatus.BAD_REQUEST);
            }
            if (endDate.isAfter(nextDateLimit)) {
                return new ResponseEntity<>("End date is after next sprints start date / project end date", HttpStatus.BAD_REQUEST);
            }

            sprint.setName(sprintInfo.getSprintName());
            sprint.setStartDate(startDate);
            sprint.setEndDate(endDate);
            sprint.setDescription(sprintInfo.getSprintDescription());
            sprint.setColour(sprintInfo.getSprintColour());
            sprintRepository.save(sprint);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception err) {
            logger.error("POST REQUEST /sprintSubmit", err);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    /**
     * Checks the SprintRequest DTO is all good and correct
     *
     * @param sprintRequest the SprintRequest to check
     * @return ResponseEntity which is either okay, or not with message.
     */
    private ResponseEntity<Object> checkSprintRequest(SprintRequest sprintRequest) {
        try {
            String sprintName = sprintRequest.getSprintName();
            LocalDate sprintStartDate = LocalDate.parse(sprintRequest.getSprintStartDate());
            LocalDate sprintEndDate = LocalDate.parse(sprintRequest.getSprintEndDate());
            String sprintDescription = sprintRequest.getSprintDescription();
            String sprintColour = sprintRequest.getSprintColour();

            if (!regexPatterns.getTitleRegex().matcher(sprintName).matches()) {
                return new ResponseEntity<>("Sprint Name not in correct format", HttpStatus.BAD_REQUEST);
            }

            if (!regexPatterns.getDescriptionRegex().matcher(sprintDescription).matches()) {
                return new ResponseEntity<>("Sprint Description not in correct format", HttpStatus.BAD_REQUEST);
            }

            if (!regexPatterns.getHexRegex().matcher(sprintColour).matches()) {
                return new ResponseEntity<>("Sprint Colour not in correct hex format", HttpStatus.BAD_REQUEST);
            }

            if (sprintEndDate.isBefore(sprintStartDate)) {
                return new ResponseEntity<>("Sprint end date is before sprint start date", HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (DateTimeParseException err) {
            return new ResponseEntity<>("Date(s) is in incorrect format", HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Mapping for delete request "deleteSprint"
     *
     * @param id UUID of sprint to delete
     * @return Confirmation of delete
     */
    @DeleteMapping("deleteSprint")
    public ResponseEntity<String> deleteSprint(@RequestParam(value = "sprintId") UUID id) {
        logger.info("DELETE REQUEST /deleteSprint");
        sprintRepository.deleteById(String.valueOf(id));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    /////////////////////////////////////////////// Test Values  ////////////////////////////////////////////////////////

    public void createDefaultEvents(Project project) throws InvalidNameException {

        Event event1 = new Event(project, "Term Break", LocalDateTime.parse("2022-04-11T08:00:00"), LocalDate.parse("2022-05-01"), LocalTime.parse("08:30:00"), 1);
        Event event2 = new Event(project, "Melbourne Grand Prix", LocalDateTime.parse("2022-04-10T17:00:00"), LocalDate.parse("2022-04-10"), LocalTime.parse("20:30:00"), 5);
        Event event3 = new Event(project, "Workshop Code Review", LocalDateTime.parse("2022-05-18T15:00:00"), LocalDate.parse("2022-05-18"), LocalTime.now(), 4);
        Event event4 = new Event(project, "Semester 2", LocalDateTime.parse("2022-07-18T15:00:00"), LocalDate.parse("2022-09-30"), LocalTime.now(), 6);
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
        eventRepository.save(event4);
    }


    /**
     * Creates default deadlines for a given project.
     *
     * @param project The project in which the deadlines will be stored.
     */
    public void createDefaultDeadlines(Project project) {
        try {
            Deadline deadline1 = new Deadline(project, "SENG 101 Assignment due", LocalDate.parse("2022-05-01"), LocalTime.parse("23:59:00"), 1);
            Deadline deadline2 = new Deadline(project, "Auckland Electoral Candidate", LocalDate.parse("2022-08-12"), LocalTime.parse("12:00:00"), 2);
            Deadline deadline3 = new Deadline(project, "NCEA level 3 Calculus exam", LocalDate.parse("2022-08-14"), LocalTime.parse("09:30:00"), 3);
            Deadline deadline4 = new Deadline(project, "NZ On Air Scripted General Audiences", LocalDate.parse("2022-09-29"), LocalTime.parse("16:00:00"), 4);
            deadlineRepository.save(deadline1);
            deadlineRepository.save(deadline2);
            deadlineRepository.save(deadline3);
            deadlineRepository.save(deadline4);
        } catch (InvalidNameException | DateTimeException err) {
            logger.warn("Error occurred loading default deadlines: {}", err.getMessage());
        }
    }


    public void createDefaultMilestones(Project project) throws InvalidNameException {
        Milestone milestone1 = new Milestone(project, "Finished the project!", LocalDate.parse("2022-05-01"), 1);
        Milestone milestone2 = new Milestone(project, "Lost all the money", LocalDate.parse("2022-06-01"), 2);
        Milestone milestone3 = new Milestone(project, "Wow look at that flying dog", LocalDate.parse("2022-07-01"), 3);

        milestoneRepository.save(milestone1);
        milestoneRepository.save(milestone2);
        milestoneRepository.save(milestone3);
    }


    public void createDefaultSprints(Project project) {
        Sprint sprint1 = new Sprint(project, "Sprint 1", LocalDate.parse("2022-02-28"), LocalDate.parse("2022-03-09"), "Sprint 1", "#0066cc");
        Sprint sprint2 = new Sprint(project, "Sprint 2", LocalDate.parse("2022-03-14"), LocalDate.parse("2022-03-30"), "Sprint 2", "#ffcc00");
        Sprint sprint3 = new Sprint(project, "Sprint 3", LocalDate.parse("2022-04-04"), LocalDate.parse("2022-05-11"), "Sprint 3", "#f48c06");
        Sprint sprint4 = new Sprint(project, "Sprint 4", LocalDate.parse("2022-05-16"), LocalDate.parse("2022-07-20"), "Sprint 4", "#118ab2");
        Sprint sprint5 = new Sprint(project, "Sprint 5", LocalDate.parse("2022-07-25"), LocalDate.parse("2022-08-10"), "Sprint 5", "#219ebc");
        Sprint sprint6 = new Sprint(project, "Sprint 6", LocalDate.parse("2022-08-15"), LocalDate.parse("2022-09-14"), "Sprint 6", "#f48c06");
        Sprint sprint7 = new Sprint(project, "Sprint 7", LocalDate.parse("2022-09-19"), LocalDate.parse("2022-09-30"), "Sprint 7", "#f48c06");
        sprintRepository.save(sprint1);
        sprintRepository.save(sprint2);
        sprintRepository.save(sprint3);
        sprintRepository.save(sprint4);
        sprintRepository.save(sprint5);
        sprintRepository.save(sprint6);
        sprintRepository.save(sprint7);
    }
}
