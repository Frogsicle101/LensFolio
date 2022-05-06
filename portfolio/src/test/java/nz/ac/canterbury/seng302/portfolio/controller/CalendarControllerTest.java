package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);

    @Autowired
    private SprintRepository sprintRepository;


    private final CalendarController calendarController = new CalendarController(projectRepository, sprintRepository);
    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);
    private final AuthState principal = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();


    private String joinParameters(HashMap<String, String> parameters) {
        String searchParams = "?";
        for (String key : parameters.keySet()) {
            searchParams += key + "=" + parameters.get(key) + "&";
        }
        return searchParams.substring(0, searchParams.length() - 1);
    }


    @BeforeEach
    public void beforeAll() {
        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.STUDENT);
        UserResponse user = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal, mockClientService)).thenReturn(user);
        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        when(mockClientService.getUserAccountById(userByIdRequest)).thenReturn(user);
        calendarController.setUserAccountsClientService(mockClientService);
        Project project = new Project("test");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    }

    @Test
    void testGetCalendar() {
        ModelAndView model = calendarController.getCalendar(principal, 1L);
        Assertions.assertEquals("monthlyCalendar", model.getViewName());

    }

    @Test
    void testGetCalendarWrongProjectId() {
        ModelAndView model = calendarController.getCalendar(principal, 2L);
        Assertions.assertEquals("errorPage", model.getViewName());
    }


    @Test
    void testGetProjectSprints() throws Exception {
        ResultActions result = this.mockMvc.perform(get("/getProjectSprints").param("projectId", "1"));
        result.andExpect(status().isOk());

    }

    @Test
    void testGetProjectSprintsBadParam() throws Exception {
        ResultActions result = this.mockMvc.perform(get("/getProjectSprints").param("projectId", "f"));
        result.andExpect(status().isBadRequest());

    }

    @Test
    void testGetProjectSprintsNoSprintsExist() throws Exception {
        ResultActions result = this.mockMvc.perform(get("/getProjectSprints").param("projectId", "100"));
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json"));
    }

    @Test
    void testGetProjectDetails() throws Exception {
        ResultActions result = this.mockMvc.perform(get("/getProjectDetails").param("projectId", "1"));
        result.andExpect(status().isOk());
    }

    @Test
    void testGetProjectDetailsProjectDoesNotExist() throws Exception {
        ResultActions result = this.mockMvc.perform(get("/getProjectDetails").param("projectId", "100"));
        result.andExpect(status().isNotFound());
    }
}