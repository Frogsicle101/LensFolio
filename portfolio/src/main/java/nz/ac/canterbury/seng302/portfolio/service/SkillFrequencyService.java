package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * The service bean responsible for skill frequency
 */
@Service
public class SkillFrequencyService {


    /** Holds persisted information about evidence */
    private final EvidenceRepository evidenceRepository;


    /**
     * Autowired constructor
     * @param evidenceRepository Evidence storage
     */
    @Autowired
    public SkillFrequencyService(EvidenceRepository evidenceRepository) {
        this.evidenceRepository = evidenceRepository;
    }


    /**
     * Gets a list of evidence that's associated with a skill and a user and a list of evidence for a user and
     * divides them by each other to get the frequency, then rounds down to 1 decimal place.
     * @param skill The skill object we want the frequency for
     * @param userId The id of the user that we want to find evidence for
     * @return How frequently the skill appears in the users evidence. Ranges from 0 (none of the time) to 1 (all of the time).
     */
    public double getSkillFrequency(Skill skill, Integer userId){
        List<Evidence> evidenceListAssociatedWithSkill = evidenceRepository
                .findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(userId, skill);
        List<Evidence> evidenceListForUser = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(userId);
        double value = (double) evidenceListAssociatedWithSkill.size() / evidenceListForUser.size();
        return Math.floor(value * 100) / 100;
    }


}
