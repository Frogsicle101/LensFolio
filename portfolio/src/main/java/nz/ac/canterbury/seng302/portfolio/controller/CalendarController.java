package nz.ac.canterbury.seng302.portfolio.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CalendarController {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    public CalendarController(ProjectRepository projectRepository, SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
    }

    @GetMapping("/calendar")
    public ModelAndView getCalendar(
            @AuthenticationPrincipal AuthState principal,
            HttpServletRequest request
            ) {
        ModelAndView model = new ModelAndView("monthly_calendar");
        Project project = projectRepository.getProjectById(1L);
        model.addObject("project", project);
        model.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        String ip = request.getLocalAddr();
        String url = "http://" + ip + ":9001/" + user.getProfileImagePath();
        model.addObject("profileImageUrl", url);

        return model;
    }


    @GetMapping("/getProjectSprints")
    public ResponseEntity<Object> getProjectSprints(@RequestParam(value = "projectId") Long projectId){
        List<Sprint> sprints = sprintRepository.findAllByProjectId(projectId);
        return new ResponseEntity<>(sprints, HttpStatus.OK);
    }
}

