package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.evidence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A utility class for more complex actions involving Evidence
 */
@Service
public class EvidenceService {

    static Pattern alpha = Pattern.compile("[a-zA-Z]");

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EvidenceRepository evidenceRepository;


    /**
     * Checks if the string is too short or matches the pattern provided
     * if either of these are true then it throws an exception
     *
     * @param string A string
     * @throws CheckException The exception to throw
     */
    public void checkString(String string) throws CheckException {
        Matcher matcher = alpha.matcher(string);
        if (string.length() < 2) {
            throw new CheckException("Title should be longer than 1 character");
        } else if (!matcher.find()) {
            throw new CheckException("Title shouldn't be strange");
        }
    }


    /**
     * Checks if the evidence date is within the project dates.
     * Also checks that the date isn't in the future
     * Throws a checkException if it's not.
     *
     * @param project      the project to check dates for.
     * @param evidenceDate the date of the evidence
     */
    public void checkDate(Project project, LocalDate evidenceDate) {
        if (evidenceDate.isBefore(project.getStartDateAsLocalDateTime().toLocalDate())
                || evidenceDate.isAfter(project.getEndDateAsLocalDateTime().toLocalDate())) {
            throw new CheckException("Date is outside project dates");
        }

        if (evidenceDate.isAfter(LocalDate.now())){
            throw new CheckException("Date is in the future");
        }
    }

    /**SkillRepository
     *add a list of skills to a given piece of evidence
     *
     * @param evidence - The  piece of evidence
     * @param skillNames - The list of the skills
     */
    public void addSkills(Evidence evidence, String[] skillNames) {
        for(String skillName: skillNames){
            if (skillName == null || skillName.equals("") || skillName.equals(" ")){
                continue;
            }
            Optional<Skill> optionalSkill = skillRepository.findByNameIgnoreCase(skillName);
            Skill theSkill;
            if (optionalSkill.isEmpty()) {
                Skill createSkill = new Skill(skillName);
                theSkill = skillRepository.save(createSkill);
            } else {
                theSkill = optionalSkill.get();
            }
            evidence.addSkill(theSkill);
            evidenceRepository.save(evidence);
        }
    }
}
