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
import javax.servlet.http.HttpServletRequest;
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


    @GetMapping("/getProjectDetails")
    public ResponseEntity<Object> getProject(
            @RequestParam(value="projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getProject");

            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + projectId + " was not found"
            ));
            return new ResponseEntity<>(project, HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.error("GET REQUEST /getProject", err);
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }

    public void setUserAccountsClientService(UserAccountsClientService service) {
        this.userAccountsClientService = service;
    }

}

