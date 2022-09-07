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
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceService(userAccountsClientService, projectRepository, evidenceRepository, webLinkRepository, skillRepository, regexService);
        evidence = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        when(userAccountsClientService.getUserAccountById(any())).thenReturn(UserResponse.newBuilder().setId(1).build());
        when(evidenceRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }


    @Test
    void addEvidence() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1L);
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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", links, new ArrayList<>(), new ArrayList<>(), 1L);
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

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        String title = "title";
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        long projectId = 1L;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1L);

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


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

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


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

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


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

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


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("title is shorter than the minimum length of 2 characters"));
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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

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


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("description is shorter than the minimum length of 2 characters"));
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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

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

        String title = "Test";
        String date = LocalDate.now().toString();
        String description = "Description";

        List<WebLinkDTO> webLinks = new ArrayList<>();
        webLinks.add(new WebLinkDTO("", "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));
        List<String> skills = new ArrayList<>();

        List<String> categories = new ArrayList<>();

        long projectId = 1L;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

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

        String title = "Test";
        String date = LocalDate.now().toString();
        String description = "Description";

        List<WebLinkDTO> webLinks = new ArrayList<>();
        webLinks.add(new WebLinkDTO("a".repeat(30), "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));

        List<String> skills = new ArrayList<>();

        List<String> categories = new ArrayList<>();

        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("should be 20 characters or less"));
    }

    @Test
    void testWeblinkWithIllegalSymbol() {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "Test";
        String date = LocalDate.now().toString();
        String description = "Description";

        List<WebLinkDTO> webLinks = new ArrayList<>();
        webLinks.add(new WebLinkDTO("Hazardous:☢", "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));

        List<String> skills = new ArrayList<>();

        List<String> categories = new ArrayList<>();

        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, skills, categories, projectId);

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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), skills, categories, 1L);
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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), skills, categories, 1L);
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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), categories, 1L);
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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), categories, 1L);
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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), categories, 1L);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(0, evidence.getCategories().size());
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
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_1")).thenReturn(Optional.of(usersSkill1));
        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill_1");

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillExistInDiffCase() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findByNameIgnoreCase("sKILL 1")).thenReturn(Optional.of(usersSkill1));
        List<String> listSkills = new ArrayList<>();
        listSkills.add("sKILL 1");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSkillsExist() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill 1")).thenReturn(Optional.of(usersSkill1));
        Skill usersSkill2 = new Skill(1, "Skill 2");
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill 2")).thenReturn(Optional.of(usersSkill2));

        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill 1");
        listSkills.add("Skill 2");

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(2)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillNotExist() {
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_1")).thenReturn(Optional.empty());

        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill 1");
        evidenceService.addSkills(evidence, listSkills);

        Mockito.verify(skillRepository, Mockito.times(1)).findByNameIgnoreCase(Mockito.any());
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

        Mockito.verify(skillRepository, Mockito.times(2)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSomeSkillsExistSomeNot() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill 1")).thenReturn(Optional.of(usersSkill1));
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill 2")).thenReturn(Optional.empty());

        List<String> listSkills = new ArrayList<>();
        listSkills.add("Skill 1");
        listSkills.add("Skill 2");

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(2)).findByNameIgnoreCase(Mockito.any());
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
        System.out.println(exception.getMessage());
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