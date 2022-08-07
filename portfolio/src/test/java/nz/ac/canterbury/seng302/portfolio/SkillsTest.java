package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.evidence.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SkillsTest {

    @Autowired
    EvidenceRepository evidenceRepository;

    @Autowired
    SkillRepository skillRepository;


    @Test
    void createTestEvidenceSkill() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        Skill skill = new Skill("Testing");
        skillRepository.save(skill);
        evidence.addSkill(skill);
        evidenceRepository.save(evidence);

        Evidence evidence1 = evidenceRepository.findAllByUserIdOrderByDateDesc(1).get(0);
        Assertions.assertEquals(evidence1.getTitle(), evidence.getTitle());
        Assertions.assertEquals(evidence1.getSkills().size(), evidence.getSkills().size());
        Assertions.assertEquals(1, evidence.getSkills().size());
    }


    @Test
    void createTestEvidenceSkills() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        Skill skill1 = new Skill("Testing 1");
        Skill skill2 = new Skill("Testing 2");
        Skill skill3 = new Skill("Testing 3");
        evidence.addSkill(skill1);
        evidence.addSkill(skill2);
        evidence.addSkill(skill3);
        skillRepository.save(skill1);
        skillRepository.save(skill2);
        skillRepository.save(skill3);
        evidenceRepository.save(evidence);

        Evidence evidence1 = evidenceRepository.findAllByUserIdOrderByDateDesc(1).get(0);
        Assertions.assertEquals(evidence1.getTitle(), evidence.getTitle());
        Assertions.assertEquals(evidence1.getSkills().size(), evidence.getSkills().size());
        Assertions.assertEquals(3, evidence.getSkills().size());
    }


    @Test
    void findAllSkillsByUserId() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        Evidence evidenceWithNoSkills = new Evidence(2, "test", LocalDate.now(), "test");
        Skill skill1 = new Skill("Testing 1");
        Skill skill2 = new Skill("Testing 2");
        Skill skill3 = new Skill("Testing 3");
        evidence.addSkill(skill1);
        evidence.addSkill(skill2);
        evidence.addSkill(skill3);
        skillRepository.save(skill1);
        skillRepository.save(skill2);
        skillRepository.save(skill3);
        evidenceRepository.save(evidence);
        evidenceRepository.save(evidenceWithNoSkills);

        List<Skill> skillsForUser1 = skillRepository.findSkillsByEvidenceUserId(1);
        List<Skill> skillsForUser2 = skillRepository.findSkillsByEvidenceUserId(2);
        Assertions.assertEquals(3, skillsForUser1.size());
        Assertions.assertEquals(skill1.getName(), skillsForUser1.get(0).getName());
        Assertions.assertEquals(skill2.getName(), skillsForUser1.get(1).getName());
        Assertions.assertEquals(skill3.getName(), skillsForUser1.get(2).getName());

        Assertions.assertEquals(0, skillsForUser2.size());
    }


    @Test
    void checkIfSkillsRepoCaseInsensitive() {
        Skill skill1 = new Skill("Testing 1");
        String differentCaseSearchQuery = "tesTing 1";

        skillRepository.save(skill1);

        Skill foundSkill = skillRepository.findByNameIgnoreCase(differentCaseSearchQuery).get();
        Assertions.assertNotEquals(foundSkill.getName(), differentCaseSearchQuery);
        Assertions.assertEquals(skill1.getName(), foundSkill.getName());
    }


    @Test
    void checkSkillRepositoryReturnForEmptyString() {
        Skill skill1 = new Skill("");
        String differentCaseSearchQuery = "";

        skillRepository.save(skill1);

        Optional<Skill> foundSkill = skillRepository.findByNameIgnoreCase(differentCaseSearchQuery);
        Assertions.assertTrue(foundSkill.isEmpty());
//        Assertions.assertEquals(skill1.getName(), foundSkill.getName());
    }
}
