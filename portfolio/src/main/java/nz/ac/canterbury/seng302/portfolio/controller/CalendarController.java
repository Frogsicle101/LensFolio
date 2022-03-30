package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.sprints.SprintRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
public class CalendarController {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;

    public CalendarController(ProjectRepository projectRepository, SprintRepository sprintRepository) {
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
    }

    @GetMapping("/calendar")
    public ModelAndView getCalendar() {
        ModelAndView model = new ModelAndView("monthly_calendar");
        Project project = projectRepository.getProjectById(1L);
        model.addObject("project", project);
        model.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));

        return model;
    }


    @GetMapping("/getProjectSprints")
    public ResponseEntity<Object> getProjectSprints(@RequestParam(value = "projectId") Long projectId){
        List<Sprint> sprints = sprintRepository.findAllByProjectId(projectId);
        return new ResponseEntity<>(sprints, HttpStatus.OK);
    }
}

