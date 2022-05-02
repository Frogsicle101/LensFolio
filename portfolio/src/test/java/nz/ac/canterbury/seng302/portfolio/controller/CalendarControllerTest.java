package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private SprintRepository sprintRepository;

    
    private Project project;

    @Test
    void testGetCalendar() throws Exception {
        this.mockMvc.perform(get("/calendar")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    void testGetProjectSprints() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        this.mockMvc.perform(get("/getProjectSprints").param("projectId", "1")).andDo(print()).andExpect(status().isOk());
    }


    @Test
    void testGetProjectSprintsNoReqParams() throws Exception {
        this.mockMvc.perform(get("/getProjectSprints")).andExpect(status().isBadRequest());
    }




}