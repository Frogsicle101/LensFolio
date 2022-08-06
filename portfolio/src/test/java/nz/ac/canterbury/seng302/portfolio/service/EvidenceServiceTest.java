package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.DTO.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.evidence.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.evidence.WebLinkRepository;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EvidenceServiceTest {

    private Authentication principal;

    private EvidenceService evidenceService;
    private final UserAccountsClientService userAccountsClientService = Mockito.mock(UserAccountsClientService.class);
    private final ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
    private final EvidenceRepository evidenceRepository = Mockito.mock(EvidenceRepository.class);
    private final WebLinkRepository webLinkRepository = Mockito.mock(WebLinkRepository.class);

    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceService(userAccountsClientService, projectRepository, evidenceRepository, webLinkRepository);
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