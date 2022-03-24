package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.*;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    private Project project;


    @Test
    public void testAddEventNoReqParams() throws Exception {
        this.mockMvc.perform(put("/addEvent")).andExpect(status().isBadRequest());
    }

    @Test
    public void testAddEventWithAllParams() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        HashMap<String, String> params = new HashMap<>();
        params.put("projectId", project.getId().toString());
        params.put("eventName", "TestEvent");
        params.put("eventStart", "2022-01-28T11:38:00.01");
        params.put("eventEnd", "2022-01-29T11:38:00.01");


        this.mockMvc.perform(put("/addEvent" + joinParameters(params))).andExpect(status().isOk());
    }

    @Test
    public void testAddEventWithBadEventDateParams() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        HashMap<String, String> params = new HashMap<>();
        params.put("projectId", project.getId().toString());
        params.put("eventName", "TestEvent");
        params.put("eventStart", "Hi, Im words.");
        params.put("eventEnd", "Im more words");


        this.mockMvc.perform(put("/addEvent" + joinParameters(params))).andExpect(status().isBadRequest());
    }


    @Test
    public void testAddEventWithBadProjectIdParams() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        HashMap<String, String> params = new HashMap<>();
        params.put("projectId", "notCorrect");
        params.put("eventName", "TestEvent");
        params.put("eventStart", "2022-01-28T11:38:00.01");
        params.put("eventEnd", "2022-01-29T11:38:00.01");

        ResultActions result = this.mockMvc.perform(put("/addEvent" + joinParameters(params)));

        result.andExpect(status().isBadRequest());
    }



    @Test
    public void testAddEventWithNonExistingProject() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        Long projectId = 1 + project.getId();

        HashMap<String, String> params = new HashMap<>();
        params.put("projectId", String.valueOf(projectId));
        params.put("eventName", "TestEvent");
        params.put("eventStart", "2022-01-28T11:38:00.01");
        params.put("eventEnd", "2022-01-29T11:38:00.01");

        ResultActions result = this.mockMvc.perform(put("/addEvent" + joinParameters(params)));

        result.andExpect(status().isNotFound());
    }


    private String joinParameters(HashMap<String, String> parameters) {
        String searchParams = "?";
        for (String key : parameters.keySet()) {
            searchParams += key + "=" + parameters.get(key) + "&";
        }
        return searchParams.substring(0, searchParams.length() - 1);
    }


}