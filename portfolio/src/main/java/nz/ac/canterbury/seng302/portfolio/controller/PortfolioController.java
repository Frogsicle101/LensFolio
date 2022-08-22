package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.RegexPatterns;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.*;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepoRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.service.CheckDateService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.RegexPattern;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
public class PortfolioController {

    private UserAccountsClientService userAccountsClientService;

    private final SprintRepository sprintRepository;

    private final ProjectRepository projectRepository;

    private final EventRepository eventRepository;

    private final DeadlineRepository deadlineRepository;

    private final MilestoneRepository milestoneRepository;

    private final GitRepoRepository gitRepoRepository;

    private final EvidenceRepository evidenceRepository;

    private final SkillRepository skillRepository;

    private final WebLinkRepository webLinkRepository;

    private final ProjectService projectService;

    private final RegexService regexService;

    private static final String ERROR_MESSAGE = "errorMessage";

    private final CheckDateService checkDateService = new CheckDateService();

    RegexPatterns regexPatterns = new RegexPatterns();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());



  /**
   * Autowired constructor for PortfolioController to inject the required beans
   *
   * @param sprintRepository repository
   * @param projectRepository repository
   * @param userAccountsClientService The bean used to get user information
   */
  @Autowired
  public PortfolioController(
      SprintRepository sprintRepository,
      ProjectRepository projectRepository,
      UserAccountsClientService userAccountsClientService,
      RegexService regexService) {
    this.projectRepository = projectRepository;
    this.sprintRepository = sprintRepository;
    this.userAccountsClientService = userAccountsClientService;
    this.regexService = regexService;
  }


  /**
   * Get mapping for /Portfolio
   *
   * @param principal - The Authentication of the user making the request, for authentication
   * @param projectId the ID of the project to display
   * @return returns the portfolio view, or error-page
   */
  @GetMapping("/portfolio")
  public ModelAndView getPortfolio(
      @AuthenticationPrincipal Authentication principal,
      @RequestParam(value = "projectId") long projectId) {
    try {
      logger.info("GET REQUEST /portfolio: Getting page");
      UserResponse user =
          PrincipalAttributes.getUserFromPrincipal(
              principal.getAuthState(), userAccountsClientService);
      Optional<Project> projectOptional = projectRepository.findById(projectId);
      if (projectOptional.isEmpty()) {
        throw new EntityNotFoundException("Project not found");
      }
      Project project = projectOptional.get();
      ModelAndView modelAndView = new ModelAndView("portfolio");
      // Checks what role the user has. Adds boolean object to the view so that displays can be
      // changed on the frontend.
      List<UserRole> roles = user.getRolesList();
      modelAndView.addObject(
          "userCanEdit",
          (roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR)));
      LocalDate defaultOccasionDate =
          project.getStartDate(); // Today is in a sprint, the start of the project otherwise
      if (checkDateService.dateIsInSprint(LocalDate.now(), project, sprintRepository)) {
        defaultOccasionDate = LocalDate.now();
      }
      modelAndView.addObject("project", project);
      modelAndView.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));
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
            UserResponse user =
                    PrincipalAttributes.getUserFromPrincipal(
                            principal.getAuthState(), userAccountsClientService);

            // Gets the project that the request is referring to.
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isEmpty()) {
                throw new EntityNotFoundException("Project not found");
            }
            Project project = projectOptional.get();

            // The view we are going to return.
            ModelAndView modelAndView = new ModelAndView("projectEdit");

            // Adds the project object to the view for use.
            modelAndView.addObject("project", project);

            // Values to set the max and min of datepicker inputs
            modelAndView.addObject("minStartDate", projectService.getMinStartDate(project));
            modelAndView.addObject("maxStartDate", projectService.getMaxStartDate(project));
            modelAndView.addObject("minEndDate", projectService.getMinEndDate(project));

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
            logger.info("POST REQUEST /projectEdit: user is editing project - {}", editInfo.getProjectId());



            LocalDate projectStart = LocalDate.parse(editInfo.getProjectStartDate());
            LocalDate projectEnd = LocalDate.parse(editInfo.getProjectEndDate());


            Project project = projectRepository
                    .findById(Long.parseLong(editInfo.getProjectId()))
                    .orElseThrow(() -> new EntityNotFoundException(
                                    "Project with id " + editInfo.getProjectId() + "was not found"
                                    )
                                );


            String projectName = editInfo.getProjectName();
            String projectDescription = editInfo.getProjectDescription();
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, projectName, 1, 50, "Project name");
            regexService.checkInput(
                    RegexPattern.GENERAL_UNICODE, projectDescription, 0, 200, "Project description");

            if (projectStart.isBefore(projectService.getMinStartDate(project))) {
                return new ResponseEntity<>("Project cannot start more than a year before today", HttpStatus.BAD_REQUEST);
            }

            if (projectStart.isAfter(projectService.getMaxStartDate(project))) {
                return new ResponseEntity<>("There is a sprint that starts before that date", HttpStatus.BAD_REQUEST);
            }

            if (projectEnd.isBefore(projectService.getMinEndDate(project))) {
                return new ResponseEntity<>("There is a sprint that extends after that date", HttpStatus.BAD_REQUEST);
            }

            //Updates the project's details
            project.setName(editInfo.getProjectName());
            project.setStartDate(projectStart);
            project.setEndDate(projectEnd);
            project.setDescription(editInfo.getProjectDescription());
            projectRepository.save(project);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException | CheckException err) {
            logger.error("POST REQUEST /projectEdit", err);
            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("POST REQUEST /projectEdit", err);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


  /**
   * Get mapping for portfolio/addSprint This is called when user wants to add a sprint.
   *
   * @param projectId Project to add the sprint to.
   * @return a response entity response
   */
  @GetMapping("/portfolio/addSprint")
  public ResponseEntity<Object> addSprint(@RequestParam(value = "projectId") Long projectId) {
    try {
      logger.info("GET REQUEST /portfolio/addSprint");
      Project project =
          projectRepository
              .findById(projectId)
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Project with id " + projectId + " was not found"));
      // Gets the amount of sprints belonging to the project
      int amountOfSprints = sprintRepository.findAllByProjectId(projectId).size() + 1;
      String sprintName = "Sprint " + amountOfSprints;
      CheckDateService.checkProjectHasRoomForSprints(sprintRepository, project);
      if (project.getStartDate().plusWeeks(3).isAfter(project.getEndDate())) {
        sprintRepository.save(
            new Sprint(project, sprintName, project.getStartDate(), project.getEndDate()));
      } else {
        sprintRepository.save(new Sprint(project, sprintName, project.getStartDate()));
      }
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (CheckException checkException) {
      logger.warn(checkException.getMessage());
      return new ResponseEntity<>(checkException.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception err) {
      logger.error("GET REQUEST /portfolio/addSprint", err);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  /**
   * Mapping for /sprintEdit. Looks for a sprint that matches the id and then populates the form.
   *
   * @param principal The authentication state
   * @param sprintId The sprint id
   * @return Thymeleaf template
   */
  @RequestMapping("/sprintEdit")
  public ModelAndView sprintEdit(
      @AuthenticationPrincipal Authentication principal,
      @RequestParam(value = "sprintId") String sprintId,
      @RequestParam(value = "projectId") Long projectId,
      RedirectAttributes attributes) {
    try {
      logger.info("GET REQUEST /sprintEdit");
      ModelAndView modelAndView = new ModelAndView("sprintEdit");
      UserResponse user =
          PrincipalAttributes.getUserFromPrincipal(
              principal.getAuthState(), userAccountsClientService);

      Sprint sprint =
          sprintRepository
              .findById(String.valueOf(sprintId))
              .orElseThrow(
                  () ->
                      new EntityNotFoundException(
                          "Sprint with id " + projectId.toString() + " was not found"));

      Project project = projectRepository.getProjectById(projectId);
      HashMap<String, LocalDate> neighbouringDates = checkNeighbourDatesForSprint(project, sprint);
      String textForPreviousSprint;
      String textForNextSprint;
      modelAndView.addObject("previousSprintEnd", neighbouringDates.get("previousSprintEnd"));
      if (neighbouringDates.get("previousSprintEnd").equals(project.getStartDate())) {
        textForPreviousSprint =
            "No previous sprints, Project starts on " + neighbouringDates.get("previousSprintEnd");
      } else {
        textForPreviousSprint =
            "Previous sprint ends on " + neighbouringDates.get("previousSprintEnd");
      }
      modelAndView.addObject("textForPrevSprint", textForPreviousSprint);
      modelAndView.addObject("nextSprintStart", neighbouringDates.get("nextSprintStart"));
      if (neighbouringDates.get("nextSprintStart").equals(project.getEndDate())) {
        textForNextSprint =
            "No next sprint, project ends on  " + neighbouringDates.get("nextSprintStart");
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
   * Get a list of all the sprints in a project by the project ID.
   *
   * @param projectId - The project that contains the sprints
   * @return A response entity containing the sprints and the HTTP status
   */
  @GetMapping("/getSprintList")
  public ResponseEntity<Object> getSprintList(@RequestParam(value = "projectId") Long projectId) {
    List<Sprint> sprintList = sprintRepository.findAllByProjectId(projectId);
    sprintList.sort(Comparator.comparing(Sprint::getStartDate));
    return new ResponseEntity<>(sprintList, HttpStatus.OK);
  }


  /**
   * Helper function that gets the dates that neighbour the sprint that it is given
   *
   * @param project the project the sprint is in
   * @param sprint the sprint
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
      neighbouringSprintDates.put(
          "previousSprintEnd", sprintList.get(indexOfPrevSprint).getEndDate().plusDays(1));
    } else {
      // Else adds an object to the view that limits the calendar to project start .
      neighbouringSprintDates.put("previousSprintEnd", project.getStartDate());
    }
    // Checks if the selected sprint is not the last on the list
    if (indexOfNextSprint < sprintList.size() - 1) {
      indexOfNextSprint = indexOfNextSprint + 1;
      // Adds an object to the view that limits the calendar to dates before the next sprints starts.
      neighbouringSprintDates.put(
          "nextSprintStart", sprintList.get(indexOfNextSprint).getStartDate().minusDays(1));
    } else {
      // Else adds an object to the view that limits the calendar to be before the project end.
      neighbouringSprintDates.put("nextSprintStart", project.getEndDate());
    }

    return neighbouringSprintDates;
  }


  /**
   * Takes the request to update the sprint. Tries to update the sprint then redirects user.
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
        return new ResponseEntity<>(
            "Start date is before previous sprints end date / project start date",
            HttpStatus.BAD_REQUEST);
      }
      if (endDate.isAfter(nextDateLimit)) {
        return new ResponseEntity<>(
            "End date is after next sprints start date / project end date", HttpStatus.BAD_REQUEST);
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
        return new ResponseEntity<>(
            "Sprint Description not in correct format", HttpStatus.BAD_REQUEST);
      }
      if (!regexPatterns.getHexRegex().matcher(sprintColour).matches()) {
        return new ResponseEntity<>(
            "Sprint Colour not in correct hex format", HttpStatus.BAD_REQUEST);
      }
      if (sprintEndDate.isBefore(sprintStartDate)) {
        return new ResponseEntity<>(
            "Sprint end date is before sprint start date", HttpStatus.BAD_REQUEST);
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
}
