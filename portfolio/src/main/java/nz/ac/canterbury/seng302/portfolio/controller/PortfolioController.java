package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.DTO.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.sprints.SprintRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
public class PortfolioController {




    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    boolean projectHasBeenCreated = false;
    long projectCreatedID;


    /**
     * Constructor for PortfolioController
     * @param sprintRepository repository
     * @param projectRepository repository
     */
    public PortfolioController(SprintRepository sprintRepository, ProjectRepository projectRepository) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Main entry point for portfolio.
     *
     * @param principal The authentication state
     * @param model The Thymeleaf model
     * @return Thymleaf template
     */
    @GetMapping("/portfolio")
    public ModelAndView getPortfolio(
                                  @AuthenticationPrincipal AuthState principal,
                                  Model model
    ) {
        ModelAndView modelAndView = new ModelAndView("portfolio");
        Project project = projectRepository.getProjectByName("Project Bravo");
        addModelAttributeProject(modelAndView, project);
        modelAndView.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));
        return modelAndView;
    }

    /**
     * Mapping for /editProject
     * Retrieves the Project from the project repository by the id passed in with request parameters.
     * Calls helper function and returns thymleaf template.
     *
     * @param principal The authentication state
     * @param projectId Id of project
     * @param model The Thymeleaf model
     * @return a thymeleaf template
     */
    @RequestMapping("/editProject")
    public ModelAndView edit(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam (value = "projectId") String projectId,
            Model model
    ) {
        Long longProjectId = Long.parseLong(projectId);
        ModelAndView modelAndView = new ModelAndView("projectEdit");
        addModelAttributeProject(modelAndView, projectRepository.getProjectById(longProjectId));
        return modelAndView;
    }


    /**
     * Mapping for /projectEdit
     * Called when user has edited a project and hit submit.
     * Gets the correct project and updates all the information and redirects user back to main page
     * @param request the HTTP request
     * @param response the HTTP response
     * @param principal The authentication state
     * @param editInfo the thymeleaf-created form object
     * @param model the thymeleaf model
     * @return a redirect to portfolio
     */
    @PostMapping("/projectEdit")
    public ModelAndView editDetails(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal AuthState principal,
            @ModelAttribute(name="editProjectForm") ProjectRequest editInfo,
            Model model
    ) {
        Project project = projectRepository.getProjectById(Long.parseLong(editInfo.getProjectId()));
        project.setName(editInfo.getProjectName());
        project.setStartDate(editInfo.getProjectStartDate());
        project.setEndDate(editInfo.getProjectEndDate());
        project.setDescription(editInfo.getProjectDescription());
        projectRepository.save(project);

        return new ModelAndView("redirect:/portfolio");
    }


    /**
     * Helper function to add objects to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param project The project
     * @param model The model you're adding attributes to
     */
    public void addModelAttributeProject(ModelAndView model, Project project){
        model.addObject("projectId", project.getId());
        model.addObject("projectName", project.getName());
        model.addObject("projectStart", project.getStartDate());
        model.addObject("projectEnd", project.getEndDate());
        model.addObject("projectDescription", project.getDescription());
    }


    /**
     * Mapping for GET request "getAllSprints"
     * @param projectId the project in which you want all the sprints from
     * @return List of all sprints
     */
    @GetMapping("getAllSprints")
    public List<Sprint> all(@RequestParam(value = "projectId") long projectId) {

        return sprintRepository.findAllByProjectId(projectId);

    }

    /**
     * Mapping for POST request "addSprint"
     * @param name Name of Sprint
     * @param projectId the project in which you want to add sprint too.
     * @return Returns JSON of Sprint Object
     */
    @PostMapping("addSprint")
    public Sprint addSprint(@RequestParam (value = "name") String name,
                            @RequestParam (value = "projectId") long projectId)  {

        String startDate;
        if (sprintRepository.count() > 0) {
            Iterable<Sprint> sprints = sprintRepository.findAll();
            LocalDate prevSprintEndDate = null;
            for (Sprint sprint:sprints) {
                if (prevSprintEndDate == null){
                    prevSprintEndDate = LocalDate.parse(sprint.getEndDate());
                } else {
                    if (LocalDate.parse(sprint.getEndDate()).isAfter(prevSprintEndDate)) {
                        prevSprintEndDate = LocalDate.parse(sprint.getEndDate());
                    }
                }
            }
            assert prevSprintEndDate != null;
            startDate = prevSprintEndDate.plusDays(1).toString();
        } else {
            startDate = LocalDate.now().toString();
        }


        return sprintRepository.save(new Sprint(projectId, name, startDate));
    }

    /**
     * Mapping for /sprintEdit. Looks for a sprint that matches the id
     * and then populates the form.
     * @param principal The authentication state
     * @param sprintId The sprint id
     * @param model Thymleaf model
     * @return Thymleaf template
     */
    @RequestMapping("/sprintEdit")
    public ModelAndView sprintEdit(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam (value = "sprintId") String sprintId,
            Model model
    ) {
        UUID uuidSprintId = UUID.fromString(sprintId);
        ModelAndView modelAndView = new ModelAndView("sprintEdit");
        addModelAttributeSprint(modelAndView, sprintRepository.getSprintById(uuidSprintId));
        return modelAndView;
    }

    /**
     * Takes the request to update the sprint.
     * Tries to update the sprint then redirects user.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param principal The authentication state
     * @param sprintInfo the thymeleaf-created form object
     * @param model Thymleaf model
     * @return redirect to portfolio
     */
    @PostMapping("/sprintSubmit")
    public ModelAndView updateSprint(
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     @AuthenticationPrincipal AuthState principal,
                                     @ModelAttribute(name="sprintEditForm") SprintRequest sprintInfo,
                                     Model model
    ) {

        Sprint sprint = sprintRepository.getSprintById(sprintInfo.getSprintId());
        sprint.setName(sprintInfo.getSprintName());
        sprint.setStartDate(sprintInfo.getSprintStartDate());
        sprint.setEndDate(sprintInfo.getSprintEndDate());
        sprint.setDescription(sprintInfo.getSprintDescription());
        sprint.setColour(sprintInfo.getSprintColour());
        sprintRepository.save(sprint);

        return new ModelAndView("redirect:/portfolio");
    }

    /**
     * Helper function to add objects to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param sprint The sprint
     * @param model The model you're adding attributes to
     */
    public void addModelAttributeSprint(ModelAndView model, Sprint sprint){
        model.addObject("sprintId", sprint.getId());
        model.addObject("sprintName", sprint.getName());
        model.addObject("sprintStart", sprint.getStartDate());
        model.addObject("sprintEnd", sprint.getEndDate());
        model.addObject("sprintDescription", sprint.getDescription());
        model.addObject("sprintColour", sprint.getColour());
    }


    /**
     * Mapping for PUT request "deleteSprint"
     * @param sprintId UUID of sprint to delete
     * @return Confirmation of delete
     */
   @DeleteMapping("deleteSprint")
    public ResponseEntity<String> deleteSprint(@RequestParam (value = "sprintId")UUID id) {
        sprintRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
   }



}
