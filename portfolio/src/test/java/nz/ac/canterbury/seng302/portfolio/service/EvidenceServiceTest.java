package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.DTO.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.evidence.*;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class EvidenceServiceTest {

    private Authentication principal;

    private Evidence evidence;

    private EvidenceService evidenceService;
    private final UserAccountsClientService userAccountsClientService = Mockito.mock(UserAccountsClientService.class);
    private final ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
    private final EvidenceRepository evidenceRepository = Mockito.mock(EvidenceRepository.class);
    private final WebLinkRepository webLinkRepository = Mockito.mock(WebLinkRepository.class);
    private final SkillRepository skillRepository = Mockito.mock(SkillRepository.class);


    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceService(userAccountsClientService, projectRepository, evidenceRepository, webLinkRepository, skillRepository);
        evidence = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        when(userAccountsClientService.getUserAccountById(any())).thenReturn(UserResponse.newBuilder().setId(1).build());
    }

    @Test
    void addEvidence() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), 1L);
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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", links, new ArrayList<>(), 1L);
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
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        long projectId = 1L;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), new ArrayList<>(), 1L);

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
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

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
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

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
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

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

        String title = "t";
        String date = LocalDate.now().toString();
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
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
        String description = "Description";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
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
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
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
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
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
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
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
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
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
        webLinks.add(new WebLinkDTO("", "https://csse-s302g6.canterbury.ac.nz/prod/potfolio"));
        List<String> categories = new ArrayList<>();

        long projectId = 1L;

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

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

        List<String> categories = new ArrayList<>();

        long projectId = 1L;


        EvidenceDTO evidenceDTO = new EvidenceDTO(title, date, description, webLinks, categories, projectId);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("should be 20 characters or less"));
    }


    @Test
    void addEvidenceWithNoCategories() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<String> categories = new ArrayList<>();

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), categories, 1L);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, times(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(0, evidence.getCategories().size());
    }


    @Test
    void addEvidenceWithOneCategory() throws MalformedURLException {
        setUserToStudent();

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        String title = "title";
        List<String> categories = new ArrayList<>();
        categories.add("SERVICE");

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), categories, 1L);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, times(1)).save(captor.capture());

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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), categories, 1L);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, times(1)).save(captor.capture());

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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), categories, 1L);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, times(1)).save(captor.capture());

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

        EvidenceDTO evidenceDTO = new EvidenceDTO(title, LocalDate.now().toString(), "Description", new ArrayList<>(), categories, 1L);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, times(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(0, evidence.getCategories().size());
    }


    // ----------------------------- Jillian's Tests --------------------------

    @Test
    void testAddSkillToEvidenceWhenNoSkill(){
        String emptySkills = "";
        String[] listSkills = emptySkills.split("\\s+");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.never()).findByNameIgnoreCase(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillExist(){
        Skill usersSkill1 = new Skill(1, "Skill_1");
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_1")).thenReturn(Optional.of(usersSkill1));
        String skillsNames = "Skill_1";
        String[] listSkills = skillsNames.split("\\s+");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillExistInDiffCase(){
        Skill usersSkill1 = new Skill(1, "Skill_1");
        Mockito.when(skillRepository.findByNameIgnoreCase("sKILL_1")).thenReturn(Optional.of(usersSkill1));
        String skillsNames = "sKILL_1";
        String[] listSkills = skillsNames.split("\\s+");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSkillsExist(){
        Skill usersSkill1 = new Skill(1, "Skill_1");
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_1")).thenReturn(Optional.of(usersSkill1));
        Skill usersSkill2 = new Skill(1, "Skill_2");
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_2")).thenReturn(Optional.of(usersSkill2));
        String skillsNames = "Skill_1 Skill_2";
        String[] listSkills = skillsNames.split("\\s+");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(2)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(2)).save(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillNotExist(){
        String skillsNames = "Skill_1";
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_1")).thenReturn(Optional.empty());
        String[] listSkills = skillsNames.split("\\s+");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSkillsNotExist(){
        String skillsNames = "Skill_1 Skill_2";
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_1")).thenReturn(Optional.empty());
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_2")).thenReturn(Optional.empty());
        String[] listSkills = skillsNames.split("\\s+");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(2)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(2)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSomeSkillsExistSomeNot(){
        Skill usersSkill1 = new Skill(1, "Skill_1");
        String skillsNames = "Skill_1 Skill_2";
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_1")).thenReturn(Optional.of(usersSkill1));
        Mockito.when(skillRepository.findByNameIgnoreCase("Skill_2")).thenReturn(Optional.empty());
        String[] listSkills = skillsNames.split("\\s+");
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(2)).findByNameIgnoreCase(Mockito.any());
        Mockito.verify(skillRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(2)).save(Mockito.any());
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