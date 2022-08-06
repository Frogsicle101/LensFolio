package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.DTO.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.evidence.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EvidenceServiceTest {

    private Authentication principal;

    private EvidenceService evidenceService;
    private final UserAccountsClientService userAccountsClientService = Mockito.mock(UserAccountsClientService.class);
    private final ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
    private final EvidenceRepository evidenceRepository = Mockito.mock(EvidenceRepository.class);

    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceService(userAccountsClientService, projectRepository, evidenceRepository);
        when(userAccountsClientService.getUserAccountById(any())).thenReturn(UserResponse.newBuilder().setId(1).build());
    }

    @Test
    void addEvidence() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), 1L);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, times(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(title, evidence.getTitle());
    }

    @Test
    void addEvidenceWithWeblinks() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";

        List<WebLinkDTO> links = new ArrayList<>();
        String url = "https://www.google.com";
        links.add(new WebLinkDTO("name", url));

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", links, 1L);
        evidenceService.addEvidence(principal,
                evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, times(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(url, evidence.getWebLinks().get(0).getUrl().toString());
    }

    @Test
    void testBadProjectId() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        String title = "title";

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        LocalDate.now().toString(),
                        "Description",
                        1L,
                        null
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("project id"));
    }

    @Test
    void testBadDateFormat() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        String date = "WOW this shouldn't work";

        Assertions.assertThrows(
                DateTimeParseException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        "Description",
                        1L,
                        null
                )
        );
    }

    @Test
    void testDateInFuture() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        String date = LocalDate.now().plusDays(1).toString();

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        "Description",
                        1L,
                        null
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("future"));
    }

    @Test
    void testDateOutsideProject() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        String date = LocalDate.now().minusDays(1).toString();

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        "Description",
                        1L,
                        null
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("outside project dates"));
    }

    @Test
    void testShortTitle() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "t";
        String date = LocalDate.now().toString();

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        "Description",
                        1L,
                        null
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("should be longer than 1 character"));
    }

    @Test
    void testLongTitle() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "This string is exactly 31 chars".repeat(5);
        String date = LocalDate.now().toString();

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        "Description",
                        1L,
                        null
                )
        );
        System.out.println(exception.getMessage());
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("cannot be more than 50 characters"));
    }

    @Test
    void testShortDescription() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Title";
        String date = LocalDate.now().toString();
        String description = "D";

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        description,
                        1L,
                        null
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("should be longer than 1 character"));
    }

    @Test
    void testLongDescription() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Title";
        String date = LocalDate.now().toString();
        String description = "This string is exactly 31 chars".repeat(20);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        description,
                        1L,
                        null
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("cannot be more than 500 characters"));
    }

    @Test
    void testStrangeTitle() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "_test_";
        String date = LocalDate.now().toString();
        String description = "Description";

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        description,
                        1L,
                        null
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("shouldn't be strange"));
    }

    @Test
    void testStrangeDescription() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Test";
        String date = LocalDate.now().toString();
        String description = "_description_";

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        description,
                        1L,
                        null
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("shouldn't be strange"));
    }

    @Test
    void testWeblinkWithShortName() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Test";
        String date = LocalDate.now().toString();
        String description = "Description";

        List<WebLinkDTO> webLinks = new ArrayList<>();
        webLinks.add(new WebLinkDTO("a", "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        description,
                        1L,
                        webLinks
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("should be longer than 1 character"));
    }

    @Test
    void testWeblinkWithLongName() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Test";
        String date = LocalDate.now().toString();
        String description = "Description";

        List<WebLinkDTO> webLinks = new ArrayList<>();
        webLinks.add(new WebLinkDTO("a".repeat(30), "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(
                        principal,
                        title,
                        date,
                        description,
                        1L,
                        webLinks
                )
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("should be 20 characters or less"));
    }

    private void setUserToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());
    }

}