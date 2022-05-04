package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.events.Event;
import nz.ac.canterbury.seng302.portfolio.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import org.aspectj.apache.bcel.classfile.ExceptionTable;

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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    @Autowired
    private EventRepository eventRepository;

    private Project project;

    private String joinParameters(HashMap<String, String> parameters) {
        String searchParams = "?";
        for (String key : parameters.keySet()) {
            searchParams += key + "=" + parameters.get(key) + "&";
        }
        return searchParams.substring(0, searchParams.length() - 1);
    }


    @Test
    void testAddEventNoReqParams() throws Exception {
        this.mockMvc.perform(put("/addEvent")).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEventWithBadProjectIdParams() throws Exception {
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
    void testDeleteEvent() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        Event event = eventRepository.save(new Event(project,"TestEvent", LocalDateTime.now(), LocalDateTime.now().plusDays(1)));

        String eventId = event.getId().toString();
        ResultActions result = this.mockMvc.perform(delete("/deleteEvent").param("eventId", eventId));
        result.andExpect(status().isOk());


    }

    @Test
    void testDeleteEventWhereEventDoesNotExist() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        UUID uuid = new UUID(2,5);
        ResultActions result = this.mockMvc.perform(delete("/deleteEvent").param("eventId", String.valueOf(uuid)));
        result.andExpect(status().isNotFound());


    }

    @Test
    void testDeleteEventWithBadParams() throws Exception {
        project = projectRepository.getProjectByName("Project Default");

        ResultActions result = this.mockMvc.perform(delete("/deleteEvent").param("eventId", "1"));
        result.andExpect(status().isBadRequest());

        ResultActions result1 = this.mockMvc.perform(delete("/deleteEvent").param("toothHurty", "1"));
        result1.andExpect(status().isBadRequest());


    }

    @Test
    void testEditEvent() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        Event event = eventRepository.save(new Event(project,"TestEvent", LocalDateTime.now(), LocalDateTime.now().plusDays(1)));

        HashMap<String, String> params = new HashMap<>();
        params.put("eventId", event.getId().toString());
        params.put("eventName", "ChangedName");
        params.put("eventStart", "2022-03-28T11:38:00.01");
        params.put("eventEnd", "2022-04-29T11:38:00.01");

        String eventId = event.getId().toString();
        ResultActions result = this.mockMvc.perform(post("/editEvent"+ joinParameters(params)));
        result.andExpect(status().isOk());
    }

    @Test
    void testEditEventWithNoParams() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        Event event = eventRepository.save(new Event(project,"TestEvent", LocalDateTime.now(), LocalDateTime.now().plusDays(1)));

        HashMap<String, String> params = new HashMap<>();
        params.put("eventId", event.getId().toString());
        params.put("eventName", "ChangedName");
        params.put("eventStart", "2022-03-28T11:38:00.01");
        params.put("eventEnd", "2022-04-29T11:38:00.01");

        String eventId = event.getId().toString();
        ResultActions result = this.mockMvc.perform(post("/editEvent"));
        result.andExpect(status().isBadRequest());
    }

    @Test
    void testEditEventWithWrongEventId() throws Exception {
        project = projectRepository.getProjectByName("Project Default");
        Event event = eventRepository.save(new Event(project,"TestEvent", LocalDateTime.now(), LocalDateTime.now().plusDays(1)));
        Event event2 = eventRepository.save(new Event(project,"TestEvent", LocalDateTime.now(), LocalDateTime.now().plusDays(1)));


        HashMap<String, String> params = new HashMap<>();
        params.put("eventId", event2.getId().toString());
        params.put("eventName", "ChangedName");
        params.put("eventStart", "2022-03-28T11:38:00.01");
        params.put("eventEnd", "2022-04-29T11:38:00.01");

        eventRepository.delete(event2);
        String eventId = event.getId().toString();
        ResultActions result = this.mockMvc.perform(post("/editEvent"+ joinParameters(params)));
        result.andExpect(status().isNotFound());
    }





}