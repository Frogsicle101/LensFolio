package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.events.Event;
import nz.ac.canterbury.seng302.portfolio.events.EventHelper;
import nz.ac.canterbury.seng302.portfolio.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.sprints.SprintRepository;
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

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
public class PortfolioController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;

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
    public PortfolioController(SprintRepository sprintRepository, ProjectRepository projectRepository, EventRepository eventRepository) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;

        //Below are only for testing purposes.
        this.defaultProject = projectRepository.save(new Project("Project Default"));
        createDefaultEvents(defaultProject);
        createDefaultSprints(defaultProject);
    }





    public void createDefaultEvents(Project project) {
        LocalDateTime date = LocalDateTime.now();
        Event event1 = new Event(project, "Sprint1 to Sprint2", date, date.plusWeeks(4), 1);
        Event event2 = new Event(project, "Sprint1 to Sprint4", date, date.plusWeeks(12), 2);
        Event event3 = new Event(project, "Merry Chrysler Day", date.minusDays(10), date.plusDays(20), 3);
        Event event4 = new Event(project, "Not in a sprint - Sprint 6", date.plusWeeks(19), date.plusWeeks(21), 4);
        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
        eventRepository.save(event4);
    }

    public void createDefaultSprints(Project project) {
        LocalDate date = LocalDate.now();
        Sprint sprint1 = new Sprint(project, "Sprint 1", date, date.plusWeeks(3), "Default1", "#0066cc");
        Sprint sprint2 = new Sprint(project, "Sprint 2", date.plusWeeks(3).plusDays(1), date.plusWeeks(6), "Default2", "#ffcc00");
        Sprint sprint3 = new Sprint(project, "Sprint 3", date.plusWeeks(6).plusDays(1), date.plusWeeks(9), "Default3", "#f48c06");
        Sprint sprint4 = new Sprint(project, "Sprint 4", date.plusWeeks(9).plusDays(1), date.plusWeeks(12), "Default4", "#118ab2");
        Sprint sprint5 = new Sprint(project, "Sprint 5", date.plusWeeks(12).plusDays(1), date.plusWeeks(15), "Default5", "#219ebc");
        Sprint sprint6 = new Sprint(project, "Sprint 6", date.plusWeeks(20).plusDays(1), date.plusWeeks(22), "Default6", "#f48c06");
        sprintRepository.save(sprint1);
        sprintRepository.save(sprint2);
        sprintRepository.save(sprint3);
        sprintRepository.save(sprint4);
        sprintRepository.save(sprint5);
        sprintRepository.save(sprint6);
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

            //Add the project object to the view to be accessed on the frontend.
            modelAndView.addObject("project", project);

            //Add a list of sprint objects to the view to be accessed on the frontend.
            modelAndView.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));

            //Add a list of event objects to the view to be accessed on the frontend.
            modelAndView.addObject("events", eventList);

            //Add an object that lets us access the event name restriction length on the frontend.
            modelAndView.addObject("eventNameLengthRestriction", Event.getNameLengthRestriction());

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

            //Updates the project's details
            project.setName(editInfo.getProjectName());
            project.setStartDate(projectStart);
            project.setEndDate(projectEnd);
            project.setDescription(editInfo.getProjectDescription());
            projectRepository.save(project);

            // Adds success message that is shown on the frontend after redirect
            attributes.addFlashAttribute(successMessage, "Project Updated!");

        } catch(EntityNotFoundException err) {
            logger.error("POST REQUEST /projectEdit", err);
            attributes.addFlashAttribute(errorMessage, err.getMessage());
        }catch(Exception err) {
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
