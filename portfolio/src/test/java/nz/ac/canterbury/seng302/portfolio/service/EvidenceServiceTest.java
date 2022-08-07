package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.evidence.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

public class EvidenceServiceTest {

    private EvidenceService evidenceService;

    private Evidence evidence;

    @Mock
    private SkillRepository skillRepository = Mockito.mock(SkillRepository.class);

    @Mock
    private EvidenceRepository evidenceRepository = Mockito.mock(EvidenceRepository.class);

    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceService(skillRepository, evidenceRepository);
        evidence = new Evidence(1, 2, "Title", LocalDate.now(), "description");
    }

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
}
