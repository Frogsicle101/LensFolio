package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.demodata.DataInitialisationManagerPortfolio;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLinkRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceResponseDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.UserDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = EvidenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class EvidenceControllerTest {

    private Authentication principal;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthenticateClientService authenticateClientService;

    @MockBean
    UserAccountsClientService userAccountsClientService;

    @MockBean
    EvidenceRepository evidenceRepository;

    @MockBean
    WebLinkRepository webLinkRepository;

    @MockBean
    ProjectRepository projectRepository;

    @MockBean
    EvidenceService evidenceService;

    @MockBean
    private GroupsClientService groupsClientService;

    @MockBean
    private DataInitialisationManagerPortfolio dataInitialisationManagerPortfolio;


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testAddEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        LocalDate date = LocalDate.now();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Evidence evidence = new Evidence(1, title, date, description);

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService);

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date.toString(), description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(any(), any())).thenReturn(evidence);

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(200, responseEntity.getStatusCode().value());
    }


    @Test
    void testAddEvidenceDateInFuture() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(1).toString();
        String description = "testing";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Date is in the future"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceOutsideProjectDates() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().minusDays(1).toString();
        String description = "testing";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Date is outside project dates"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceTitleEmpty() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Title should be longer than 1 character"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceTitleMixed() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "@#!@#&(*&!@#(&*!@(*&#(*!@&#(&(*&!@(*#&!@#asdasd";
        LocalDate date = LocalDate.now();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Evidence evidence = new Evidence(1, title, date, description);

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService);

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date.toString(), description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(any(), any())).thenReturn(evidence);

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(200, responseEntity.getStatusCode().value());
    }


    @Test
    void testAddEvidenceTitleLength() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "This should almost definitely be past 50 characters in length?";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Title cannot be more than 50 characters"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceTitleNoAlpha() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "@@@";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Title shouldn't be strange"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescriptionEmpty() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "testing";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Text should be longer than 1 character"));

        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescriptionMixed() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        LocalDate date = LocalDate.now();
        String description = "@#!@#&(*&!@#(&*!@(*&#(*!@&#(&(*&!@(*#&!@#asdasd";
        long projectId = 1;
        Project project = new Project("Testing");
        Evidence evidence = new Evidence(1, title, date, description);

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService);

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date.toString(), description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(any(), any())).thenReturn(evidence);

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(200, responseEntity.getStatusCode().value());
    }


    @Test
    void testAddEvidenceDescriptionLength() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "This should almost definitely be past 500 characters in length?                                                                                                                                                                                                                                                                      This should almost definitely be past 500 characters in length?                                                                                                                                                                                                                                                                      ";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Description cannot be more than 500 characters"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescriptionNoAlpha() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "Test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "@@@";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Text shouldn't be strange"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescription() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "title";
        LocalDate date = LocalDate.now();
        String description = "Description";
        long projectId = 1;
        Project project = new Project("Testing");
        Evidence evidence = new Evidence(1, title, date, description);

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService);

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date.toString(), description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(any(), any())).thenReturn(evidence);

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(200, responseEntity.getStatusCode().value());
    }


    @Test
    void testAddEvidenceProjectId() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Project Id does not match any project"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDate() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = "WOW this shouldn't work";
        String description = "testing";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new DateTimeParseException("test", "test", 0));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDateNoDate() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = "";
        String description = "testing";
        long projectId = 1;
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new DateTimeParseException("test", "test", 0));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceException() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = "WOW this shouldn't work";
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService);

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), projectId, new ArrayList<>());
        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(principal, evidenceDTO)).thenThrow(new RuntimeException());

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(500, responseEntity.getStatusCode().value());
    }

    @Test
    void testAddEvidenceInvalidAssociateId() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> associateIds = new ArrayList<>(List.of("5", "My dear friend Joe"));
        long projectId = 1;
        mockMvc.perform(post("/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"" + title + "\", \"date\": \"" + date + "\", " +
                                "\"description\": \"" + description + "\", \"webLinks\": " + webLinks + ", " +
                                "\"categories\": " + categories + ", \"skills\": " + skills + ", " +
                                "\"associateIds\": " + associateIds + ", \"projectId\": \"" + projectId + "\"" +
                                "}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    // ------------------------------------ GET evidence tests -------------------------------------

    @Test
    void TestGetEvidenceWhenUserExistsAndHasNoEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";
        String expectedContent = "[]";

        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(new ArrayList<>());

        MvcResult result = mockMvc.perform(get("/evidenceData")
                .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserExistsAndHasOneEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(1, 2, "Title", LocalDate.now()
, "description");
        usersEvidence.add(evidence);

        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        EvidenceResponseDTO expectedResponse = new EvidenceResponseDTO(evidence);
        String expectedContent = "[" + expectedResponse.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserExistsAndHasMultipleEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence1 = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        Evidence evidence2 = new Evidence(3, 4, "Title 2", LocalDate.now(), "description 2");
        usersEvidence.add(evidence1);
        usersEvidence.add(evidence2);

        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andReturn();
        EvidenceResponseDTO expectedResponse1 = new EvidenceResponseDTO(evidence1);
        EvidenceResponseDTO expectedResponse2 = new EvidenceResponseDTO(evidence2);

        String expectedContent = "[" + expectedResponse1.toJsonString() + "," + expectedResponse2.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserDoesntExistsReturnsStatusNotFound() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String notExistingUserId = "2";

        mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", notExistingUserId))
                .andExpect(status().isNotFound());
    }


    @Test
    void TestGetEvidenceWhenBadUserIdReturnsStatusBadRequest() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String illegalUserId = "IllegalId";

        mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", illegalUserId))
                .andExpect(status().isBadRequest());
    }


    @Test
    void TestGetEvidenceReturnsBadRequestWhenNoIdIncluded() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();

        mockMvc.perform(get("/evidenceData"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TestGetEvidenceWhenUserExistsAndHasNoAssociates() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(1, 1, "Title", LocalDate.now()
                , "description");
        usersEvidence.add(evidence);

        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        EvidenceResponseDTO expectedResponse = new EvidenceResponseDTO(evidence);
        String expectedContent = "[" + expectedResponse.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }

    @Test
    void TestGetEvidenceWhenUserExistsAndHasOneAssociate() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(1, 1, "Title", LocalDate.now()
                , "description");
        usersEvidence.add(evidence);
        evidence.addAssociateId(1); // The user themselves should be considered an associate

        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(1).build();
        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setId(1)
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
        UserResponse userResponse = userBuilder.build();
        when(userAccountsClientService.getUserAccountById(request)).thenReturn(userResponse);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);


        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        UserDTO expectedUser = new UserDTO(userResponse);
        EvidenceResponseDTO expectedResponse = new EvidenceResponseDTO(evidence, List.of(expectedUser));
        String expectedContent = "[" + expectedResponse.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }

    @Test
    void TestGetEvidenceWhenUserExistsAndHasMultipleAssociates() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(1, 1, "Title", LocalDate.now()
                , "description");
        usersEvidence.add(evidence);
        evidence.addAssociateId(1); // The user themselves should be considered an associate
        evidence.addAssociateId(2);
        evidence.addAssociateId(3);

        List<UserDTO> expectedUsers = new ArrayList<>();
        for (int i = 1 ; i <= 3 ; i++) {
            UserResponse.Builder userBuilder = UserResponse.newBuilder().setId(i);
            userBuilder.addRoles(UserRole.STUDENT);
            UserResponse userResponse = userBuilder.build();

            GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(i).build();
            when(userAccountsClientService.getUserAccountById(request)).thenReturn(userResponse);

            UserDTO expectedUser = new UserDTO(userResponse);
            expectedUsers.add(expectedUser);
        }
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();


        EvidenceResponseDTO expectedResponse = new EvidenceResponseDTO(evidence, expectedUsers);
        String expectedContent = "[" + expectedResponse.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    // -------------- WebLink Tests ---------------------------------------------------------------


    @Test
    void TestGetSingleWebLinkValidId() throws Exception {
        setUserToStudent();
        setUpContext();
        int evidenceId = 1;
        Evidence evidence1 = new Evidence(evidenceId, 1, "Title", LocalDate.now(), "description");
        WebLink testLink = new WebLink(evidence1, "test link", new URL("https://www.canterbury.ac.nz/"));
        evidence1.addWebLink(testLink);
        when(evidenceRepository.findById(any())).thenReturn(Optional.of(evidence1));

        MvcResult result = mockMvc.perform(get("/evidencePieceWebLinks")
                        .queryParam("evidenceId", String.valueOf(evidenceId)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResult = "[" + testLink.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedResult, responseContent);
    }


    @Test
    void TestGetSingleWebLinkInvalidId() throws Exception {
        setUserToStudent();
        setUpContext();
        int evidenceId = 1;
        Evidence evidence1 = new Evidence(evidenceId, 1, "Title", LocalDate.now(), "description");
        WebLink testLink = new WebLink(evidence1, "test link", new URL("https://www.canterbury.ac.nz/"));
        evidence1.addWebLink(testLink);
        when(evidenceRepository.findById(evidenceId)).thenReturn(Optional.of(evidence1));

        mockMvc.perform(get("/evidencePieceWebLinks")
                        .queryParam("evidenceId", "Invalid ID"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void TestGetSingleWebLinkNoEvidence() throws Exception {
        setUserToStudent();
        setUpContext();

        MvcResult result = mockMvc.perform(get("/evidencePieceWebLinks")
                        .queryParam("evidenceId", "1"))
                .andExpect(status().isNotFound())
                .andReturn();
    }


    @Test
    void TestGetMultipleWebLinkValidId() throws Exception {
        setUserToStudent();
        setUpContext();
        int evidenceId = 1;
        Evidence evidence1 = new Evidence(evidenceId, 1, "Title", LocalDate.now(), "description");
        WebLink testLink1 = new WebLink(evidence1, "test link 1", new URL("https://www.canterbury.ac.nz/"));
        WebLink testLink2 = new WebLink(evidence1, "test link 2", new URL("https://www.canterbury.ac.nz/"));
        WebLink testLink3 = new WebLink(evidence1, "test link 3", new URL("https://www.canterbury.ac.nz/"));
        evidence1.addWebLink(testLink1);
        evidence1.addWebLink(testLink2);
        evidence1.addWebLink(testLink3);
        when(evidenceRepository.findById(any())).thenReturn(Optional.of(evidence1));

        MvcResult result = mockMvc.perform(get("/evidencePieceWebLinks")
                        .queryParam("evidenceId", String.valueOf(evidenceId)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResult = "[" + testLink1.toJsonString() + "," + testLink2.toJsonString() + ","
                + testLink3.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseContent.contains(testLink1.toJsonString()));
        Assertions.assertTrue(responseContent.contains(testLink2.toJsonString()));
        Assertions.assertTrue(responseContent.contains(testLink3.toJsonString()));
    }

    @Test
    void TestValidateWebLinkValid() throws Exception {
        mockMvc.perform(post("/validateWebLink")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"A Test Weblink\", \"url\": \"https://www.canterbury.ac.nz/\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void TestValidateWebLinkInvalidURLNoProtocol() throws Exception {
        mockMvc.perform(post("/validateWebLink")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"A Test Weblink\", \"url\": \"www.canterbury.ac.nz/\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TestValidateWebLinkInvalidURLIllegalCharactersNonBreakingSpace() throws Exception {
        mockMvc.perform(post("/validateWebLink")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"A Test Weblink\", \"url\": \"https://www.google.com&nbsp;\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TestValidateWebLinkInvalidURLIllegalCharactersHTMLTags() throws Exception {
        mockMvc.perform(post("/validateWebLink")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"A Test Weblink\", \"url\": \"https://www.<script>Something naughty!</script>place.com\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    // -------------- Helper context functions ----------------------------------------------------


    private void setUpContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));

        SecurityContextHolder.setContext(mockedSecurityContext);
    }


    private void setUserToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setId(1)
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
        UserResponse userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

    }


    private void initialiseGetRequestMocks() {
        GetUserByIdRequest existingUserRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        UserResponse userResponse = UserResponse.newBuilder().setId(1).build();
        Mockito.when(userAccountsClientService.getUserAccountById(existingUserRequest)).thenReturn(userResponse);

        GetUserByIdRequest nonExistentUserRequest = GetUserByIdRequest.newBuilder().setId(2).build();
        UserResponse notFoundResponse = UserResponse.newBuilder().setId(-1).build();
        Mockito.when(userAccountsClientService.getUserAccountById(nonExistentUserRequest)).thenReturn(notFoundResponse);
    }
}
