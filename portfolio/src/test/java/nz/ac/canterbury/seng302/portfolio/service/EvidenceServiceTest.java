package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.*;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EvidenceServiceTest {

    private final UserAccountsClientService userAccountsClientService = Mockito.mock(UserAccountsClientService.class);
    private final ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
    private final EvidenceRepository evidenceRepository = Mockito.mock(EvidenceRepository.class);
    private final WebLinkRepository webLinkRepository = Mockito.mock(WebLinkRepository.class);
    private final SkillRepository skillRepository = Mockito.mock(SkillRepository.class);
    private final RegexService regexService = Mockito.spy(RegexService.class);
    private Authentication principal;
    private Evidence evidence;
    private EvidenceService evidenceService;
    private EvidenceDTO evidenceDTO;
    private Project project;

    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceService(userAccountsClientService, projectRepository, evidenceRepository, webLinkRepository, skillRepository, regexService);
        evidence = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        when(userAccountsClientService.getUserAccountById(any())).thenReturn(UserResponse.newBuilder().setId(1).build());
        when(evidenceRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(skillRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        evidenceService = Mockito.spy(evidenceService);
    }


    @Test
    void addEvidence() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1L, new ArrayList<>());
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", links, new ArrayList<>(), new ArrayList<>(), 1L, new ArrayList<>());
        evidenceService.addEvidence(principal,
                evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(url, evidence.getWebLinks().iterator().next().getUrl().toString());
    }


    @Test
    void testBadProjectId() {
        setUserToStudent();

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        String title = "title";

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1L, List.of());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
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
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, List.of());

        Assertions.assertThrows(
                DateTimeParseException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
    }


    @Test
    void testDateInFuture() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));


        String title = "title";
        String date = LocalDate.now().plusDays(1).toString();
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, List.of());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
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
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, List.of());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("outside project dates"));
    }


    @Test
    void testShortTitle() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "";
        String date = LocalDate.now().toString();
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, List.of());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("title is shorter than the minimum length of 5 characters"));
    }


    @Test
    void testLongTitle() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "This string is exactly 31 chars".repeat(5);
        String date = LocalDate.now().toString();
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, List.of());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        System.out.println(exception.getMessage());
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("title is longer than the maximum length of 50 characters"));
    }


    @Test
    void testShortDescription() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Title";
        String date = LocalDate.now().toString();
        String description = "";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, List.of());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("description is shorter than the minimum length of 5 characters"));
    }


    @Test
    void testLongDescription() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Title";
        String date = LocalDate.now().toString();
        String description = "This string is exactly 31 chars".repeat(20);
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, List.of());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        System.out.println(exception.getMessage().toLowerCase());
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("description is longer than the maximum length of 500 characters"));
    }


    @Test
    void testWeblinkWithShortName() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Title";
        String date = LocalDate.now().toString();
        String description = "Description";

        List<WebLinkDTO> webLinks = new ArrayList<>();
        webLinks.add(new WebLinkDTO("", "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));
        List<String> skills = new ArrayList<>();

        List<String> categories = new ArrayList<>();

        long projectId = 1L;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, new ArrayList<>());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("name should be at least 1 character in length"));
    }


    @Test
    void testWeblinkWithLongName() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Title";
        String date = LocalDate.now().toString();
        String description = "Description";

        List<WebLinkDTO> webLinks = new ArrayList<>();
        webLinks.add(new WebLinkDTO("a".repeat(WebLink.MAXNAMELENGTH + 1), "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));

        List<String> skills = new ArrayList<>();

        List<String> categories = new ArrayList<>();

        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, new ArrayList<>());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("should be 50 characters or less"));
    }

    @Test
    void testWeblinkWithIllegalSymbol() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Title";
        String date = LocalDate.now().toString();
        String description = "Description";

        List<WebLinkDTO> webLinks = new ArrayList<>();
        webLinks.add(new WebLinkDTO("Hazardous:☢", "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));

        List<String> skills = new ArrayList<>();

        List<String> categories = new ArrayList<>();

        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId, new ArrayList<>());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("web link name can only contain unicode " +
                "letters, numbers, punctuation, symbols (but not emojis) and whitespace"));
    }


    @Test
    void addEvidenceWithNoCategories() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<String> skills = new ArrayList<>();

        List<String> categories = new ArrayList<>();

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), skills, categories, 1L, new ArrayList<>());
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(0, evidence.getCategories().size());
    }


    @Test
    void addEvidenceWithOneCategory() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<String> skills = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        categories.add("SERVICE");

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), skills, categories, 1L, new ArrayList<>());
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(1, evidence.getCategories().size());
        Assertions.assertTrue(evidence.getCategories().contains(Category.SERVICE));
    }


    @Test
    void addEvidenceWithAllCategories() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<String> categories = new ArrayList<>();
        categories.add("SERVICE");
        categories.add("QUANTITATIVE");
        categories.add("QUALITATIVE");

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), categories, 1L, new ArrayList<>());
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(3, evidence.getCategories().size());
        Assertions.assertTrue(evidence.getCategories().contains(Category.SERVICE));
        Assertions.assertTrue(evidence.getCategories().contains(Category.QUANTITATIVE));
        Assertions.assertTrue(evidence.getCategories().contains(Category.QUALITATIVE));
    }


    @Test
    void addEvidenceCategoriesCantBeAddedTwice() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<String> categories = new ArrayList<>();
        categories.add("SERVICE");
        categories.add("QUALITATIVE");
        categories.add("QUALITATIVE");

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), categories, 1L, new ArrayList<>());
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(2, evidence.getCategories().size());
        Assertions.assertTrue(evidence.getCategories().contains(Category.SERVICE));
        Assertions.assertTrue(evidence.getCategories().contains(Category.QUALITATIVE));
    }


    @Test
    void addEvidenceCategoriesDoesNothingWithNotExistingCategories() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<String> categories = new ArrayList<>();
        categories.add("NOT");

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), categories, 1L, new ArrayList<>());
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(0, evidence.getCategories().size());
    }

    @Test
    void addEvidenceWithAssociatedUsers() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<Integer> associates = new ArrayList<>(List.of(1, 12, 13, 14));
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1L, associates);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        // Verify that it saved more than usual - currently evidenceRepository.save is called two times per user id
        Mockito.verify(evidenceRepository, times(associates.size() * 2)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(4, evidence.getAssociateIds().size());
        Assertions.assertEquals(associates, evidence.getAssociateIds());
        Assertions.assertTrue(associates.contains(evidence.getUserId()));
    }

    @Test
    void addEvidenceWithNoAssociatedUsers() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<Integer> associates = new ArrayList<>(List.of(1));
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1L, associates);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        // Verify that it saved more than usual - currently evidenceRepository.save is called three times per user id
        Mockito.verify(evidenceRepository, times(associates.size() * 2)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(1, evidence.getAssociateIds().size()); // The creator is considered an associate, so expected size is 1
        Assertions.assertEquals(associates, evidence.getAssociateIds());
        Assertions.assertTrue(associates.contains(evidence.getUserId()));
    }

    @Test
    void addEvidenceWithDuplicateAssociatedUsers() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<Integer> associates = new ArrayList<>(List.of(1, 12, 13, 14, 12));
        List<Integer> expectedAssociates = new ArrayList<>(List.of(1, 12, 13, 14));
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1L, associates);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        // Verify that it saved more than usual - currently evidenceRepository.save is called three times per user id
        Mockito.verify(evidenceRepository, times((associates.size() - 1) * 2)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(4, evidence.getAssociateIds().size()); // The creator is considered an associate, so expected size is 1
        Assertions.assertEquals(expectedAssociates, evidence.getAssociateIds());
        Assertions.assertTrue(expectedAssociates.contains(evidence.getUserId()));
    }

    @Test
    void addEvidenceWithAssociatedUsersInvalidAssociateId() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<Integer> associates = new ArrayList<>(List.of(12, 13, -14));
        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(-14).build();
        when(userAccountsClientService.getUserAccountById(request)).thenReturn(UserResponse.newBuilder().setId(-1).build());
        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1L, associates);

        Assertions.assertThrows(CheckException.class, () -> evidenceService.addEvidence(principal, evidenceDTO));
    }




    // ----------------------------- Add Skill Tests --------------------------


    @Test
    void testAddSkillToEvidenceWhenNoSkill() {
        List<String> listSkills = new ArrayList<>();
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.never()).findByNameIgnoreCase(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillExist() {
        Skill usersSkill1 = new Skill(1, "Skill_1");
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("Skill_1"))).thenReturn(Optional.of(usersSkill1));
        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill_1");

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillExistInDiffCase() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("sKILL 1"))).thenReturn(Optional.of(usersSkill1));
        List<String> listSkills = new ArrayList<>();
        listSkills.add("sKILL 1");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSkillsExist() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("Skill 1"))).thenReturn(Optional.of(usersSkill1));
        Skill usersSkill2 = new Skill(1, "Skill 2");
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("Skill 2"))).thenReturn(Optional.of(usersSkill2));

        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill 1");
        listSkills.add("Skill 2");

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(2)).findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillNotExist() {
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("Skill_1"))).thenReturn(Optional.empty());

        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill 1");
        evidenceService.addSkills(evidence, listSkills);

        Mockito.verify(skillRepository, Mockito.times(1)).findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), Mockito.any());
        Mockito.verify(skillRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSkillsNotExist() {
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill 1")).thenReturn(Optional.empty());
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill 2")).thenReturn(Optional.empty());
        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill 1");
        listSkills.add("Skill 2");
        evidenceService.addSkills(evidence, listSkills);

        Mockito.verify(skillRepository, Mockito.times(2)).findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), Mockito.any());
        Mockito.verify(skillRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSomeSkillsExistSomeNot() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("Skill 1"))).thenReturn(Optional.of(usersSkill1));
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("Skill 2"))).thenReturn(Optional.empty());

        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill 1");
        listSkills.add("Skill 2");

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(2)).findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), Mockito.any());
        Mockito.verify(skillRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillNameTooShort() {
        List<String> listSkills = new ArrayList<>();
        listSkills.add("");

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addSkills(evidence, listSkills)
        );
        Assertions.assertTrue(exception.getMessage().contains("is shorter than the minimum length of"));
    }

    @Test
    void testAddSkillNameTooLong() {
        List<String> listSkills = new ArrayList<>();
        listSkills.add("A Decently Long Skill Name, Which as of the time of writing " +
                "should exceed the limit of thirty characters");

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addSkills(evidence, listSkills)
        );
        Assertions.assertTrue(exception.getMessage().contains("is longer than the maximum length of"));
    }

    @Test
    void testAddSkillNameContainsIllegalSymbol() {
        List<String> listSkills = new ArrayList<>();
        listSkills.add("Dangerous Skill: ☢");

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addSkills(evidence, listSkills)
        );
        Assertions.assertTrue(exception.getMessage().contains("Skill name can only contain unicode letters, numbers, " +
                "punctuation, symbols (but not emojis) and whitespace"));
    }


    @Test
    void testSkillSavesUniquelyToUser() {
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("SKILL"))).thenReturn(Optional.empty());

        List<String> newSkill = new ArrayList<>();
        newSkill.add("SKILL");
        evidenceService.addSkills(evidence, newSkill);

        Mockito.verify(skillRepository, Mockito.times(1)).save(Mockito.any());
    }


    private void setupEditEvidenceTests() throws Exception {
        evidenceDTO = new EvidenceDTO.EvidenceDTOBuilder()
                .setId(10)
                .setTitle("New Title")
                .setDate(LocalDate.now().toString())
                .setDescription("New description")
                .setWebLinks(new ArrayList<>(
                        Arrays.asList(
                                new WebLinkDTO("New weblink 1", "http://www.google.com"),
                                new WebLinkDTO("New weblink 2", "https://localhost:9000/test")
                        )))
                .setCategories(new ArrayList<>(
                        Arrays.asList("SERVICE", "QUANTITATIVE"
                        )))
                .setSkills(new ArrayList<>(
                        Arrays.asList("Testing", "Backend")
                ))
                .setAssociateIds(new ArrayList<>(
                        Arrays.asList(2, 3, 4, 5)
                ))
                .setProjectId(1L)
                .build();
        evidence = new Evidence(10,
                                1,
                                "Test Original title",
                                LocalDate.now().minusDays(1) ,
                                "Test Original Description");
        evidence.addWebLink(new WebLink(evidence, "Original Link", new URL("https://localhost:8080")));
        evidence.addSkill(new Skill("Java"));
        evidence.addCategory(Category.QUALITATIVE);
        evidence.addCategory(Category.QUANTITATIVE);
        evidence.addAssociateId(2);
        // Adds the archived ID
        evidence.addAssociateId(3);
        evidence.removeAssociateId(3);

        project = new Project("Project title");
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), any())).thenReturn(Optional.empty());
        Mockito.when(evidenceRepository.findById(evidenceDTO.getId())).thenReturn(Optional.of(evidence));
        Mockito.when(projectRepository.findById(Mockito.any())).thenReturn(Optional.of(project));
    }


    private void assertEvidenceDtoMatchesEvidence(Integer userId) {
        Assertions.assertEquals(evidence.getId(), evidenceDTO.getId());
        Assertions.assertEquals(evidence.getUserId(), userId);
        Assertions.assertEquals(evidence.getTitle(), evidenceDTO.getTitle());
        Assertions.assertEquals(evidence.getDate(), LocalDate.parse(evidenceDTO.getDate()));
        Assertions.assertEquals(evidence.getDescription(), evidenceDTO.getDescription());
        Assertions.assertEquals(evidence.getWebLinks().size(), evidenceDTO.getWebLinks().size());
        for (WebLinkDTO webLinkDTO : evidenceDTO.getWebLinks()) {
            Assertions.assertTrue(evidence.getWebLinks().stream().anyMatch(link -> link.getAlias().equals(webLinkDTO.getName())));
        }
        System.out.println(evidence.getSkills());
        System.out.println(evidenceDTO.getSkills());
        Assertions.assertEquals(evidence.getSkills().size(), evidenceDTO.getSkills().size());
        for (String skillString : evidenceDTO.getSkills()) {
            Assertions.assertTrue(evidence.getSkills().stream().anyMatch(skill -> skill.getName().equals(skillString)));
        }
        Assertions.assertEquals(evidence.getCategories().size(), evidenceDTO.getCategories().size());
        for (String categoryString : evidenceDTO.getCategories()) {
            switch (categoryString) {
                case "QUANTITATIVE" -> Assertions.assertTrue(evidence.getCategories().contains(Category.QUANTITATIVE));
                case "QUALITATIVE" -> Assertions.assertTrue(evidence.getCategories().contains(Category.QUALITATIVE));
                case "SERVICE" -> Assertions.assertTrue(evidence.getCategories().contains(Category.SERVICE));
            }
        }
        Assertions.assertEquals(evidence.getAssociateIds().size(), evidenceDTO.getAssociateIds().size());
        for (Integer associateId : evidenceDTO.getAssociateIds()) {
            Assertions.assertTrue(evidence.getAssociateIds().contains(associateId));
        }
    }


    @Test
    void testEditServiceUpdatesWhenAllFieldsAreValid() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();
        Integer originalEvidenceUserId = evidence.getUserId();

        evidenceService.editEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        evidence = captor.getValue();

        assertEvidenceDtoMatchesEvidence(originalEvidenceUserId);
    }


    @Test
    void testEditEvidenceWhenUserDoesntOwnTheEvidence() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();
        evidence.setUserId(2);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.editEvidence(principal, evidenceDTO)
        );
        Mockito.verify(evidenceRepository, never()).save(any());
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("owned by a different user"));
    }


    @Test
    void testEditEvidenceWhenEvidenceDoesntExist() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        Mockito.when(evidenceRepository.findById(evidenceDTO.getId())).thenReturn(Optional.empty());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.editEvidence(principal, evidenceDTO)
        );
        Mockito.verify(evidenceRepository, never()).save(any());
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("no evidence found"));
    }


    @Test
    void testRequiredValidationIsCalledOnEvidenceEdit() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        evidenceService.editEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Mockito.verify(regexService, times(1)).checkInput(eq(RegexPattern.GENERAL_UNICODE), any(), anyInt(), anyInt(), eq("Title"));
        Mockito.verify(regexService, times(1)).checkInput(eq(RegexPattern.GENERAL_UNICODE), any(), anyInt(), anyInt(), eq("Description"));
        Mockito.verify(evidenceService, times(1)).checkValidEvidenceDTO(evidenceDTO);
        evidence = captor.getValue();
    }


    @Test
    void testOnlyTheRightUsersGetNewEvidenceOnEdit() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        evidenceService.editEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        List<Evidence> capturedEvidence = captor.getAllValues();
        Set<Integer> usersWithSavedEvidence = capturedEvidence.stream().map(Evidence::getUserId).collect(Collectors.toSet());
        Set<Integer> expectedUsersToHaveSave = new HashSet<>(Arrays.asList(1, 4, 5));

        for (Integer user : usersWithSavedEvidence) {
            Assertions.assertTrue(expectedUsersToHaveSave.contains(user));
        }
        Assertions.assertEquals(expectedUsersToHaveSave.size(), usersWithSavedEvidence.size());
    }

    // ---------------------------------------------------


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