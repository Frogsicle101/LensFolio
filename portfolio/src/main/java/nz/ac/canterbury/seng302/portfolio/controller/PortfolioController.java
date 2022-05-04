package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineHelper;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventHelper;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneHelper;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.InvalidNameException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


@RestController
public class PortfolioController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;
    private final DeadlineRepository deadlineRepository;
    private final MilestoneRepository milestoneRepository;

    //Selectors for the error/info/success boxes.
    private static final String errorMessage = "errorMessage";
    private static final String infoMessage = "infoMessage";
    private static final String successMessage = "successMessage";

    //below is for testing purposes
    private final Project defaultProject;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Constructor for PortfolioController
     * @param sprintRepository repository
     * @param projectRepository repository
     */
    public PortfolioController(SprintRepository sprintRepository, ProjectRepository projectRepository, EventRepository eventRepository, DeadlineRepository deadlineRepository, MilestoneRepository milestoneRepository) throws InvalidNameException {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
        this.deadlineRepository = deadlineRepository;
        this.milestoneRepository = milestoneRepository;

        //Below are only for testing purposes.
        this.defaultProject = projectRepository.save(new Project("Project Seng302", LocalDate.parse("2022-02-25"), LocalDate.parse("2022-09-30"), "SENG302 is all about putting all that you have learnt in other courses into a systematic development process to create software as a team."));
        createDefaultEvents(defaultProject);
        createDefaultSprints(defaultProject);
        createDefaultMilestones(defaultProject);
    }


    /**
     * Creates events for a given project.
     *
     * @param project The project in which the events will be stored.
     * @throws InvalidNameException If the event name is null or longer than 50 characters.
     */
    public void createDefaultEvents(Project project) throws InvalidNameException {
        LocalDateTime date = LocalDateTime.now();

        Event event1 = new Event(project, "Term Break", LocalDateTime.parse("2022-04-11T08:00:00"), LocalDate.parse("2022-05-01"), LocalTime.parse("08:00:00"), 1);
        Event event2 = new Event(project, "Melbourne Grand Prix", LocalDateTime.parse("2022-04-10T17:00:00"), LocalDate.parse("2022-04-10"), LocalTime.parse("19:00:00"), 5);
        Event event3 = new Event(project, "Workshop Code Review", LocalDateTime.parse("2022-05-18T15:00:00"), LocalDate.parse("2022-05-18"), LocalTime.parse("17:00:00"), 4);
        Event event4 = new Event(project, "Semester 2", LocalDateTime.parse("2022-07-18T15:00:00"), LocalDate.parse("2022-09-30"), LocalTime.parse("17:00:00"), 6);
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
        eventRepository.save(event4);
    }

    public void createDefaultDeadlines(Project project) throws InvalidNameException {
        Deadline deadline1 = new Deadline(project, "SENG 101 Assignment due", LocalDate.parse("2022-05-01"), LocalTime.parse("23:59:00"), 1);
        Deadline deadline2 = new Deadline(project, "Auckland Electoral Candidate Entries Close", LocalDate.parse("2022-08-12"), LocalTime.parse("12:00:00"), 2);
        Deadline deadline3 = new Deadline(project, "NCEA level 3 Calculus exam", LocalDate.parse("2022-10-14"), LocalTime.parse("09:30:00"), 3);
        Deadline deadline4 = new Deadline(project, "NZ On Air Scripted General Audiences Applics close", LocalDate.parse("2022-09-29"), LocalTime.parse("16:00:00"), 4);
        deadlineRepository.save(deadline1);
        deadlineRepository.save(deadline2);
        deadlineRepository.save(deadline3);
        deadlineRepository.save(deadline4);
    }

    public void createDefaultMilestones(Project project) throws InvalidNameException {
        Milestone milestone1 = new Milestone(project, "Last date to withdraw from SENG 302", LocalDate.parse("2022-05-15"),4);
        Milestone milestone2 = new Milestone(project, "Vic Uni applications close", LocalDate.parse("2022-05-15"),4);
        Milestone milestone3 = new Milestone(project, "100 days of SENG 302", LocalDate.parse("2022-06-04"),4);
        Milestone milestone4 = new Milestone(project, "100 days to go SENG 302", LocalDate.parse("2022-07-06"),4);
        milestoneRepository.save(milestone1);
        milestoneRepository.save(milestone2);
        milestoneRepository.save(milestone3);
        milestoneRepository.save(milestone4);
    }


    /**
     * Created sprints for a given project.
     * @param project The project in which the sprints will be stored.
     */
    public void createDefaultSprints(Project project) {
        LocalDate date = LocalDate.now();
        Sprint sprint1 = new Sprint(project, "Sprint 1", LocalDate.parse("2022-02-28"), LocalDate.parse("2022-03-09"), "Sprint 1", "#0066cc");
        Sprint sprint2 = new Sprint(project, "Sprint 2", LocalDate.parse("2022-03-14"), LocalDate.parse("2022-03-30"), "Sprint 2", "#ffcc00");
        Sprint sprint3 = new Sprint(project, "Sprint 3", LocalDate.parse("2022-04-04"), LocalDate.parse("2022-05-11"), "Sprint 3", "#f48c06");
        Sprint sprint4 = new Sprint(project, "Sprint 4", LocalDate.parse("2022-05-16"), LocalDate.parse("2022-07-20"), "Sprint 4", "#118ab2");
        Sprint sprint5 = new Sprint(project, "Sprint 5", LocalDate.parse("2022-07-25"), LocalDate.parse("2022-08-10"), "Sprint 5", "#219ebc");
        Sprint sprint6 = new Sprint(project, "Sprint 6",  LocalDate.parse("2022-08-15"), LocalDate.parse("2022-09-14"), "Sprint 6", "#f48c06");
        Sprint sprint7 = new Sprint(project, "Sprint 7",  LocalDate.parse("2022-09-19"), LocalDate.parse("2022-09-30"), "Sprint 7", "#f48c06");
        sprintRepository.save(sprint1);
        sprintRepository.save(sprint2);
        sprintRepository.save(sprint3);
        sprintRepository.save(sprint4);
        sprintRepository.save(sprint5);
        sprintRepository.save(sprint6);
        sprintRepository.save(sprint7);
    }


    /**
     * Get mapping for /Portfolio
     * @param principal - The AuthState of the user making the request, for authentication
     * @param projectId Id of the project to display
     * @return returns the portfolio view, or error-page
     */
    @GetMapping("/portfolio")
    public ModelAndView getPortfolio(
                                  @AuthenticationPrincipal AuthState principal,
                                  @RequestParam(value = "projectId") long projectId,
                                  HttpServletRequest request
    ) {
        try {

            logger.info("GET REQUEST /portfolio");

            // Get user from server
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);


            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + projectId + " was not found"
            ));


            //View that we are going to return.
            ModelAndView modelAndView = new ModelAndView("portfolio");

            // Checks what role the user has. Adds boolean object to the view so that displays can be changed on the frontend.
            List<UserRole> roles = user.getRolesList();
            if (roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR)) {
                modelAndView.addObject("userCanEdit", true);
            } else {
                modelAndView.addObject("userCanEdit", false);
            }
            //Creates the list of events for the front end.
            List<Event> eventList = EventHelper.setEventColours(project.getId(), eventRepository, sprintRepository);
            List<Milestone> milestoneList = MilestoneHelper.setMilestoneColours(project.getId(), milestoneRepository, sprintRepository);
            List<Deadline> deadlineList = DeadlineHelper.setDeadlineColours(project.getId(), deadlineRepository, sprintRepository);

            //Add the project object to the view to be accessed on the frontend.
            modelAndView.addObject("project", project);

            //Add a list of sprint objects to the view to be accessed on the frontend.
            modelAndView.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));

            //Add a list of event objects to the view to be accessed on the frontend.
            modelAndView.addObject("events", eventList);

            //Add a list of milestone objects to the view to be accessed on the frontend.
            modelAndView.addObject("milestones", milestoneList);

            //Add a list of deadline objects to the view to be accessed on the frontend.
            modelAndView.addObject("deadlines", deadlineList);

            //Add an object that lets us access the event name restriction length on the frontend.
            modelAndView.addObject("occasionNameLengthRestriction", Event.getNameLengthRestriction());

            //Add the user object to the view to be accessed on the front end.
            modelAndView.addObject("username", user.getUsername());

            //TESTING PURPOSES. Passes the projectId to the front end. This will be removed when there is a way
            //to have each user select what project they want to go to from the navbar.
            modelAndView.addObject("projectId", projectId);

            // For setting the profile image
            String ip = request.getLocalAddr();
            String url = "http://" + ip + ":9001/" + user.getProfileImagePath();
            modelAndView.addObject("profileImageUrl", url);


            return modelAndView;

        } catch(EntityNotFoundException err) {
            logger.error("GET REQUEST /portfolio", err);
            return new ModelAndView("errorPage").addObject(errorMessage, err.getMessage());
        }
        catch(Exception err) {
            logger.error("GET REQUEST /portfolio", err);
            return new ModelAndView("errorPage").addObject(errorMessage, err);
        }

    }

    /**
     * Request mapping for /editProject
     * @param principal - The AuthState of the user making the request, for authentication
     * @param projectId The project to edit
     * @return Returns the project edit page or the error page
     */
    @RequestMapping("/editProject")
    public ModelAndView edit(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam (value = "projectId") Long projectId,
            HttpServletRequest request
    ) {
        try{
            logger.info("GET REQUEST /editProject");

            // Get user from server
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + projectId + "was not found"
            ));


            // The view we are going to return.
            ModelAndView modelAndView = new ModelAndView("projectEdit");

            // Adds the project object to the view for use.
            modelAndView.addObject("project", project);

            // Adds the username to the view for use.
            modelAndView.addObject("username", user.getUsername());

            // For setting the profile image
            String ip = request.getLocalAddr();
            String url = "http://" + ip + ":9001/" + user.getProfileImagePath();
            modelAndView.addObject("profileImageUrl", url);

            return modelAndView;

        }catch(EntityNotFoundException err) {
            logger.error("GET REQUEST /editProject", err);
            return new ModelAndView("errorPage").addObject(errorMessage, err);
        } catch(Exception err) {
            logger.error("GET REQUEST /editProject", err);
            return new ModelAndView("errorPage");
        }

    }


    /**
     * Postmapping for /projectEdit, this is called when user submits there project changes.
     * @param editInfo A DTO of project from the inputs on the edit page.
     * @param attributes attributes that we can add stuff to display errors/info on view that it returns.
     * @return Returns to the portfolio page.
     */
    @PostMapping("/projectEdit")
    public ModelAndView editDetails(
            @ModelAttribute(name="editProjectForm") ProjectRequest editInfo,
            RedirectAttributes attributes
        ) {
        try {

            logger.info("POST REQUEST /projectEdit");
            LocalDate projectStart = LocalDate.parse(editInfo.getProjectStartDate());
            LocalDate projectEnd = LocalDate.parse(editInfo.getProjectEndDate());


            Project project = projectRepository.findById(Long.parseLong(editInfo.getProjectId())).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + editInfo.getProjectId() + "was not found"
            ));

            List<Sprint> sprintListEndDates = sprintRepository.getAllByProjectOrderByEndDateDesc(project);
            List<Sprint> sprintListStartDates = sprintRepository.getAllByProjectOrderByStartDateAsc(project);
            if (!sprintListEndDates.isEmpty()) {
                Sprint sprint = sprintListEndDates.get(0);
                if (sprint.getEndDate().isAfter(projectEnd)) {
                    attributes.addFlashAttribute(errorMessage, "Could not change project dates.  New project end date of" + projectEnd.toString() + " is before the sprint: " + sprint.getName() + " ends: " + sprint.getEndDate().toString());
                    return new ModelAndView("redirect:/portfolio?projectId=" + editInfo.getProjectId());
                }
                sprint = sprintListStartDates.get(0);
                if (sprint.getStartDate().isBefore(projectStart)){
                    attributes.addFlashAttribute(errorMessage, "Could not change project dates. New project start date of: " + projectStart.toString() + " is after the sprint: " + sprint.getName() + " starts: " + sprint.getStartDate().toString());
                    return new ModelAndView("redirect:/portfolio?projectId=" + editInfo.getProjectId());
                }
            }


            //Updates the project's details
            project.setName(editInfo.getProjectName());
            project.setStartDate(projectStart);
            project.setEndDate(projectEnd);
            project.setDescription(editInfo.getProjectDescription());
            projectRepository.save(project);

            // Adds success message that is shown on the frontend after redirect
            attributes.addFlashAttribute(successMessage, "Project Updated!");


        } catch (EntityNotFoundException err) {
            logger.error("POST REQUEST /projectEdit", err);
            attributes.addFlashAttribute(errorMessage, err.getMessage());
        } catch (Exception err) {
            logger.error("POST REQUEST /projectEdit", err);
            return new ModelAndView("errorPage").addObject(errorMessage, err);
        }
        return new ModelAndView("redirect:/portfolio?projectId=" + editInfo.getProjectId());
    }

    /**
     * Get mapping for portfolio/addSprint
     * This is called when user wants to add a sprint.
     * @param projectId Project to add the sprint to.
     * @param attributes Attributes we can use to return errors/info.
     * @return Either the portfolio page, or the error page.
     */
    @GetMapping("/portfolio/addSprint")
    public ModelAndView addSprint(
            @RequestParam (value = "projectId") Long projectId,
            RedirectAttributes attributes)  {
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
                attributes.addFlashAttribute(errorMessage, "No more room to add sprints within project dates!");
            } else {
                // Check that if the end date (startDate.plus(3)weeks) is after project end date, then set the end date
                // to be the project end date.
                 if (startDate.plusWeeks(3).isAfter(project.getEndDate())) {
                    //Save the new sprint
                    sprintRepository.save(new Sprint(project, sprintName, startDate, project.getEndDate()));
                }else {
                    //Save the new sprint
                    sprintRepository.save(new Sprint(project, sprintName, startDate));
                }
                attributes.addFlashAttribute(successMessage, "Sprint added!");
            }
        } catch(EntityNotFoundException err) {
            logger.error("GET REQUEST /portfolio/addSprint", err);
            attributes.addFlashAttribute(errorMessage, err.getMessage());
            return new ModelAndView("error");
        }catch(Exception err) {
            logger.error("GET REQUEST /portfolio/addSprint", err);
            attributes.addFlashAttribute(errorMessage, err);
            return new ModelAndView("error");
        }

        return new ModelAndView("redirect:/portfolio?projectId=" + projectId);
    }


    /**
     * Mapping for /sprintEdit. Looks for a sprint that matches the id
     * and then populates the form.
     * @param principal The authentication state
     * @param sprintId The sprint id
     * @return Thymleaf template
     */
    @RequestMapping("/sprintEdit")
    public ModelAndView sprintEdit(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam (value = "sprintId") String sprintId,
            @RequestParam (value = "projectId") Long projectId,
            RedirectAttributes attributes
    ) {
        try {

            logger.info("GET REQUEST /sprintEdit");
            ModelAndView modelAndView = new ModelAndView("sprintEdit");

            // Get user from server
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);


            UUID uuidSprintId = UUID.fromString(sprintId);

            Sprint sprint = sprintRepository.findById(uuidSprintId).orElseThrow(() -> new EntityNotFoundException(
                    "Sprint with id " + projectId.toString() + " was not found"
            ));

            Project project = projectRepository.getProjectById(projectId);

            // Gets a list of all sprints that belong to the project and orders them by start date: earliest to latest
            List<Sprint> sprintList = sprintRepository.getAllByProjectOrderByStartDateAsc(project);

            int indexOfPrevSprint = sprintList.indexOf(sprint);
            int indexOfNextSprint = sprintList.indexOf(sprint);

            // Checks if the selected sprint is not the first on the list
            if (indexOfPrevSprint > 0) {
                indexOfPrevSprint = indexOfPrevSprint - 1;
                // Adds an object to the view that limits the calendar to dates past the previous sprints end.
                modelAndView.addObject("previousSprintEnd", sprintList.get(indexOfPrevSprint).getEndDate().plusDays(1));
                String textForPreviousSprint = "Previous sprint ends on " + sprintList.get(indexOfPrevSprint).getEndDateFormatted();
                modelAndView.addObject("textForPrevSprint", textForPreviousSprint);
            } else {
                // Else adds an object to the view that limits the calendar to project start .
                modelAndView.addObject("previousSprintEnd", project.getStartDate());
            }
            // Checks if the selected sprint is not the last on the list
            if (indexOfNextSprint < sprintList.size() - 1) {
                indexOfNextSprint = indexOfNextSprint + 1;
                // Adds an object to the view that limits the calendar to dates before the next sprints starts.
                modelAndView.addObject("nextSprintStart", sprintList.get(indexOfNextSprint).getStartDate().minusDays(1));
                String textForNextSprint = "Next sprint starts on " + sprintList.get(indexOfNextSprint).getStartDateFormatted();
                modelAndView.addObject("textForNextSprint", textForNextSprint);
            } else {
                // Else adds an object to the view that limits the calendar to be before the project end.
                modelAndView.addObject("nextSprintStart", project.getEndDate());
            }

            // Adds the username to the view for use.
            modelAndView.addObject("username", user.getUsername());

            // Add the sprint to the view for use.
            modelAndView.addObject("sprint", sprint);


            return modelAndView;
        } catch(Exception err) {
            logger.error("GET REQUEST /sprintEdit", err);
            attributes.addFlashAttribute(errorMessage, err);
            return new ModelAndView("redirect:/portfolio?projectId=" + projectId);
        }
    }


    /**
     * Takes the request to update the sprint.
     * Tries to update the sprint then redirects user.
     * @param sprintInfo the thymeleaf-created form object
     * @return redirect to portfolio
     */
    @PostMapping("/sprintSubmit")
    public ModelAndView updateSprint(
            RedirectAttributes attributes,
            @ModelAttribute(name="sprintEditForm") SprintRequest sprintInfo) {

        try {
            logger.info("POST REQUEST /sprintSubmit");
            Project project = sprintRepository.getSprintById(sprintInfo.getSprintId()).getProject();

            LocalDate sprintStart = LocalDate.parse(sprintInfo.getSprintStartDate());
            LocalDate sprintEnd = LocalDate.parse(sprintInfo.getSprintEndDate());
            if (sprintStart.isAfter(sprintEnd)) {
                String dateErrorMessage = "Start date needs to be before end date";
                attributes.addFlashAttribute(errorMessage, dateErrorMessage);
            } else {
                Sprint sprint = sprintRepository.getSprintById(sprintInfo.getSprintId());
                sprint.setName(sprintInfo.getSprintName());
                sprint.setStartDate(sprintStart);
                sprint.setEndDate(sprintEnd);
                sprint.setDescription(sprintInfo.getSprintDescription());
                sprint.setColour(sprintInfo.getSprintColour());
                sprintRepository.save(sprint);
                return new ModelAndView("redirect:/portfolio?projectId=" + project.getId());
            }
            return new ModelAndView("redirect:/sprintEdit?sprintId=" + sprintInfo.getSprintId());

        } catch(Exception err) {
            logger.error("POST REQUEST /sprintSubmit", err);
            attributes.addFlashAttribute(errorMessage, err);
            return new ModelAndView("redirect:/sprintEdit?sprintId=" + sprintInfo.getSprintId());
        }
    }


    /**
     * Mapping for PUT request "deleteSprint"
     * @param id UUID of sprint to delete
     * @return Confirmation of delete
     */
   @DeleteMapping("deleteSprint")
    public ResponseEntity<String> deleteSprint(@RequestParam (value = "sprintId")UUID id) {
       logger.info("DELETE REQUEST /deleteSprint");
        sprintRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
   }
}
