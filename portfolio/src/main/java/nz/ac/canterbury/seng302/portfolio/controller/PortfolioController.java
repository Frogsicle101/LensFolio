package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.DTO.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.sprints.SprintRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;


import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.UUID;


@RestController
public class PortfolioController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    //Selectors for the error/info/success boxes.
    private final String errorMessage = "errorMessage";
    private final String infoMessage = "infoMessage";
    private final String successMessage = "successMessage";





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
     * @return Thymeleaf template
     */
    @GetMapping("/portfolio")
    public ModelAndView getPortfolio(
                                  @AuthenticationPrincipal AuthState principal
    ) {





        // Get user from server
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        ModelAndView modelAndView = new ModelAndView("portfolio");
        //TODO Change the below line so that it isn't just grabbing one single project?.
        Project project = projectRepository.getProjectById(2L);
        addModelAttributeProject(modelAndView, project, user);
        modelAndView.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));
        return modelAndView;
    }

    /**
     * Mapping for /editProject
     * Retrieves the Project from the project repository by the id passed in with request parameters.
     * Calls helper function and returns thymeleaf template.
     *
     * @param principal The authentication state
     * @param projectId Id of project
     * @return a thymeleaf template
     */
    @RequestMapping("/editProject")
    public ModelAndView edit(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam (value = "projectId") String projectId
    ) {
        // Get user from server
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        Long longProjectId = Long.parseLong(projectId);
        Project project = projectRepository.getProjectById(longProjectId);
        ModelAndView modelAndView = new ModelAndView("projectEdit");
        addModelAttributeProject(modelAndView, project, user);
        return modelAndView;
    }


    /**
     * Mapping for /projectEdit
     * Called when user has edited a project and hit submit.
     * Gets the correct project and updates all the information and redirects user back to main page
     * @param editInfo the thymeleaf-created form object
     * @return a redirect to portfolio
     */
    @PostMapping("/projectEdit")
    public ModelAndView editDetails(
            @ModelAttribute(name="editProjectForm") ProjectRequest editInfo,
            RedirectAttributes attributes
    ) {
        try {
            Project project = projectRepository.getProjectById(Long.parseLong(editInfo.getProjectId()));
            project.setName(editInfo.getProjectName());
            project.setStartDate(editInfo.getProjectStartDate());
            project.setEndDate(editInfo.getProjectEndDate());
            project.setDescription(editInfo.getProjectDescription());
            projectRepository.save(project);
            attributes.addFlashAttribute(successMessage, "Project Updated!");
        } catch(Exception err) {
           attributes.addFlashAttribute(errorMessage, err);
        }
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
    public void addModelAttributeProject(ModelAndView model, Project project, UserResponse user){
        model.addObject("projectId", project.getId());
        model.addObject("projectName", project.getName());
        model.addObject("projectStart", project.getStartDate());
        model.addObject("projectEnd", project.getEndDate());
        model.addObject("projectDescription", project.getDescription());
        model.addObject("username", user.getUsername());
    }



    /**
     * Mapping for POST request "addSprint"
     * @param projectId the project in which you want to add sprint too.
     * @return Returns JSON of Sprint Object
     */
    @GetMapping("/portfolio/addSprint")
    public ModelAndView addSprint(
            @RequestParam (value = "projectId") String projectId,
            RedirectAttributes attributes)  {
        try {
            long longProjectId = Long.parseLong(projectId);
            int amountOfSprints = sprintRepository.findAllByProjectId(longProjectId).size() + 1;
            String sprintName = "Sprint " + amountOfSprints;
            String startDate;

            //If there are sprints in the repository, start date of added sprint is after the last sprints end date.
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
            sprintRepository.save(new Sprint(longProjectId, sprintName, startDate));
            attributes.addFlashAttribute(successMessage, "Sprint added!");
        } catch(Exception err) {
            attributes.addFlashAttribute(errorMessage, err);
        }

        return new ModelAndView("redirect:/portfolio");
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
            RedirectAttributes attributes
    ) {
        // Get user from server
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        try {
            UUID uuidSprintId = UUID.fromString(sprintId);
            ModelAndView modelAndView = new ModelAndView("sprintEdit");
            addModelAttributeSprint(modelAndView, sprintRepository.getSprintById(uuidSprintId), user);
            return modelAndView;
        } catch(Exception err) {
            attributes.addFlashAttribute(errorMessage, err);
            return new ModelAndView("redirect:/portfolio");
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
            LocalDate sprintStart = LocalDate.parse(sprintInfo.getSprintStartDate());
            LocalDate sprintEnd = LocalDate.parse(sprintInfo.getSprintEndDate());
            if (sprintStart.isAfter(sprintEnd)) {
                String dateErrorMessage = "Start date needs to be before end date";
                attributes.addFlashAttribute(errorMessage, dateErrorMessage);
            } else {
                Sprint sprint = sprintRepository.getSprintById(sprintInfo.getSprintId());
                sprint.setName(sprintInfo.getSprintName());
                sprint.setStartDate(sprintInfo.getSprintStartDate());
                sprint.setEndDate(sprintInfo.getSprintEndDate());
                sprint.setDescription(sprintInfo.getSprintDescription());
                sprint.setColour(sprintInfo.getSprintColour());
                sprintRepository.save(sprint);
                return new ModelAndView("redirect:/portfolio");
            }
            return new ModelAndView("redirect:/sprintEdit?sprintId=" + sprintInfo.getSprintId());

        } catch(Exception err) {

            attributes.addFlashAttribute(errorMessage, err);
            return new ModelAndView("redirect:/sprintEdit?sprintId=" + sprintInfo.getSprintId());
        }
    }

    /**
     * Helper function to add objects to the model
     * Given a Thymeleaf model, adds a bunch of attributes into it
     *
     * This is really just to make the code a bit nicer to look at
     * @param sprint The sprint
     * @param model The model you're adding attributes to
     */
    public void addModelAttributeSprint(ModelAndView model, Sprint sprint, UserResponse user){
        model.addObject("sprintId", sprint.getId());
        model.addObject("sprintName", sprint.getName());
        model.addObject("sprintStart", sprint.getStartDate());
        model.addObject("sprintEnd", sprint.getEndDate());
        model.addObject("sprintDescription", sprint.getDescription());
        model.addObject("sprintColour", sprint.getColour());
        model.addObject("username", user.getUsername());
    }


    /**
     * Mapping for PUT request "deleteSprint"
     * @param id UUID of sprint to delete
     * @return Confirmation of delete
     */
   @DeleteMapping("deleteSprint")
    public ResponseEntity<String> deleteSprint(@RequestParam (value = "sprintId")UUID id) {
        sprintRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
   }



}
