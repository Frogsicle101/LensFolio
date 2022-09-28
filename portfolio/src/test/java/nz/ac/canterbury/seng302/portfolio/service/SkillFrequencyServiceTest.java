package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import static org.mockito.Mockito.spy;

class SkillFrequencyServiceTest {

    private final EvidenceRepository evidenceRepository = spy(EvidenceRepository.class);
    private Skill skill;


    @InjectMocks
    private SkillFrequencyService skillFrequencyService = new SkillFrequencyService(evidenceRepository);


    @Test
    void testFrequency(){
        ArrayList<Evidence> evidenceListWithSkill = createEvidenceList(5, 10, true);
        ArrayList<Evidence> evidenceListTotal = createEvidenceList(5, 10, false);
        Mockito.when(evidenceRepository.findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(1, skill)).thenReturn(evidenceListWithSkill);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(evidenceListTotal);
        double frequency = skillFrequencyService.getSkillFrequency(skill, 1);
    Assertions.assertEquals(0.5, frequency);
    }

    @Test
    void testFrequencyPointTwo(){
        ArrayList<Evidence> evidenceListWithSkill = createEvidenceList(20, 100, true);
        ArrayList<Evidence> evidenceListTotal = createEvidenceList(20, 100, false);
        Mockito.when(evidenceRepository.findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(1, skill)).thenReturn(evidenceListWithSkill);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(evidenceListTotal);
        double frequency = skillFrequencyService.getSkillFrequency(skill, 1);
        Assertions.assertEquals(0.2, frequency);
    }



    ArrayList<Evidence> createEvidenceList(int amountOfEvidenceWithSkill, int amountOfEvidenceWithoutSkill, boolean returnJustSkills) {
        skill = new Skill("test");
        ArrayList<Evidence> evidenceList = new ArrayList<>();
        for(int i = 0; i < amountOfEvidenceWithSkill; i++) {
            Evidence evidence = new Evidence(1, "evidence" + i, LocalDate.now(), "test evidence");
            evidence.addSkill(skill);
            evidenceList.add(evidence);
        }
        if (returnJustSkills){
            return evidenceList;
        }

        for(int i = amountOfEvidenceWithSkill; i < amountOfEvidenceWithoutSkill; i++) {
            Evidence evidence = new Evidence(1, "evidence non skill" + i, LocalDate.now(), "test evidence");
            evidenceList.add(evidence);
        }
        return evidenceList;
    }


}
