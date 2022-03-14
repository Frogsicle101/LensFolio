package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.sprints.SprintRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
     * Mapping for GET request /portfolio
     * @return portfolio.html
     */
    @GetMapping("/portfolio")
    public ModelAndView portfolio(@AuthenticationPrincipal AuthState principal, Model model) {

        String role = principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("role"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");

        Integer id = Integer.valueOf(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("-100"));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("portfolio");

        model.addAttribute("role", role);
        model.addAttribute("id", id);
        model.addAttribute("name", principal.getName());
        model.addAttribute("authenticated", principal.getIsAuthenticated());



        return modelAndView;
    }

    @GetMapping("getProject")
    public Project getProjectById(){
        if (projectRepository.count() > 0 && !projectHasBeenCreated) {
            projectHasBeenCreated = true;
            projectCreatedID = 2;
        }
        if (projectHasBeenCreated) {
            return projectRepository.getProjectById(projectCreatedID);
        }

        Project newProject = new Project(1, "Project ".concat(String.valueOf(LocalDate.now().getYear())));
        projectCreatedID = projectRepository.save(newProject).getId();
        projectHasBeenCreated = true;
        return projectRepository.getProjectById(projectCreatedID);


    }


    @PutMapping("editProject")
    public ResponseEntity<String> updateProject(@RequestParam (value = "id") Long id,
                                              @RequestParam (value = "name") String name,
                                              @RequestParam (value = "startDate")String startDate,
                                              @RequestParam (value = "endDate") String endDate,
                                              @RequestParam (value = "description") String description){

        LocalDate sDate = LocalDate.parse(startDate);
        LocalDate eDate = LocalDate.parse(endDate);
        if (eDate.isBefore(sDate)) {
            return new ResponseEntity<>("End Date is before Start Date",HttpStatus.NOT_ACCEPTABLE);
        }
        if (name.isEmpty()) {
            return new ResponseEntity<>("Please enter a name", HttpStatus.NOT_ACCEPTABLE);
        }
        if (sDate.isBefore(LocalDate.now().minusYears(1))) {
            return new ResponseEntity<>("Project cannot start more than a year ago", HttpStatus.NOT_ACCEPTABLE);
        }
        else {
            Project projectToChange = projectRepository.findById(id).get();
            projectToChange.setDescription(description);
            projectToChange.setStartDate(startDate);
            projectToChange.setEndDate(endDate);
            projectToChange.setName(name);
            projectRepository.save(projectToChange);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }

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
     * Mapping for PUT request "editSprint"
     * @param id UUID of sprint
     * @param name Name of sprint
     * @param projectId Project Id of Sprint
     * @param startDate Start Date of Sprint
     * @param endDate End Date of Sprint
     * @param description Description of Sprint
     * @param colour Colour of Sprint
     * @return Response entity
     */
   @PutMapping("editSprint")
   public ResponseEntity<String>updateSprint(@RequestParam (value = "id") UUID id,
                              @RequestParam (value = "name") String name,
                              @RequestParam (value = "projectId") long projectId,
                              @RequestParam (value = "startDate")String startDate ,
                              @RequestParam (value = "endDate") String endDate,
                              @RequestParam (value = "description") String description,
                              @RequestParam (value = "colour") String colour){

       LocalDate sDate = LocalDate.parse(startDate);
       LocalDate eDate = LocalDate.parse(endDate);

       Sprint sprintToChange = sprintRepository.findById(id).get();
       Iterable<Sprint> sprints = sprintRepository.findAllByIdNot(id);
       for (Sprint sp:sprints) {
           LocalDate prevSprintEndDate = LocalDate.parse(sp.getEndDate());
           if (sDate.isBefore(prevSprintEndDate) && eDate.isAfter(prevSprintEndDate)) {
               return new ResponseEntity<>("Oops, looks like this sprint has date conflict",HttpStatus.NOT_ACCEPTABLE);
           }
       }

        if (eDate.isBefore(sDate)) {
            return new ResponseEntity<>("End Date is before Start Date",HttpStatus.NOT_ACCEPTABLE);
        }
        if (name.isEmpty()) {
            return new ResponseEntity<>("Please enter a name", HttpStatus.NOT_ACCEPTABLE);
        }

         else {

            sprintToChange.setColour(colour);
            sprintToChange.setDescription(description);
            sprintToChange.setStartDate(startDate);
            sprintToChange.setEndDate(endDate);
            sprintToChange.setName(name);
            sprintRepository.save(sprintToChange);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }

   }

    /**
     * Mapping for PUT request "deleteSprint"
     * @param id UUID of sprint to delete
     * @return Confirmation of delete
     */
   @DeleteMapping("deleteSprint")
    public ResponseEntity<String> deleteSprint(@RequestParam (value = "id")UUID id) {
        sprintRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
   }



}
