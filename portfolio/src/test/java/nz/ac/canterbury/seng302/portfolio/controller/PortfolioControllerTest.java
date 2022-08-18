package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.PortfolioApplication;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.SkillRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLinkRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepoRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepository;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = PortfolioController.class)
@AutoConfigureMockMvc(addFilters = false)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Authentication principal;

    @MockBean
    private AuthenticateClientService authenticateClientService;

    @MockBean
    private UserAccountsClientService userAccountsClientService;

    @MockBean
    private GroupsClientService groupsClientService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private SprintRepository sprintRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private MilestoneRepository milestoneRepository;

    @MockBean
    private DeadlineRepository deadlineRepository;

    @MockBean
    private SkillRepository skillRepository;

    @MockBean
    private WebLinkRepository webLinkRepository;

    @MockBean
    private RegexService regexService;

    @MockBean
    private GitRepoRepository gitRepository;

    @MockBean
    private EvidenceRepository evidenceRepository;

    @MockBean
    private PortfolioController portfolioController;


    private final Integer validUserId = 1;
    private final Integer nonExistentUserId = 2;
    private final String invalidUserId = "Not an Id";

    @BeforeEach
    public void setup() {

        setUserToStudent();
        setUpContext();


    }

    @Test
    void testGetPortfolio() throws Exception {

        UserResponse validUserResponse = UserResponse.newBuilder().setId(validUserId).build();
        Mockito.when(userAccountsClientService.getUserAccountById(any())).thenReturn(validUserResponse);
        MvcResult result = mockMvc.perform(get("/portfolio").param("projectId", String.valueOf(1L))
                      )
                .andExpect(status().isOk())
                .andReturn();

    Assertions.assertEquals("", result.getResponse().);
    }


    // -------------- Helper context functions ----------------------------------------------------


    private void setUpContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));
        Project project = new Project(
                "Project Seng302",
                LocalDate.parse("2022-02-25"),
                LocalDate.parse("2022-09-30"),
                "SENG302 is all about putting all that you have learnt in"
                        + " other courses into a systematic development process to"
                        + " create software as a team.");
        Mockito.when(projectRepository.getProjectById(1L)).thenReturn(project);
        Mockito.when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        Mockito.when(projectRepository.save(Mockito.any())).thenReturn(project);
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

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userBuilder.build());
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

    }
}
