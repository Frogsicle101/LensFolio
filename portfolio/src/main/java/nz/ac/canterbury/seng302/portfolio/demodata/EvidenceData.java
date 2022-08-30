package nz.ac.canterbury.seng302.portfolio.demodata;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.time.LocalDate;

/**
 *The service to initialize the evidence, weblink and skills data.
 */
@Service
public class EvidenceData {

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository contain the evidence */
    private final EvidenceRepository evidenceRepository;

    /** The repository contain the weblinks */
    private final WebLinkRepository webLinkRepository;

    /** The repository contain the skills */
    private final SkillRepository skillRepository;


    @Autowired
    public EvidenceData(EvidenceRepository evidenceRepository,
                        WebLinkRepository webLinkRepository,
                        SkillRepository skillRepository) {
        this.evidenceRepository = evidenceRepository;
        this.webLinkRepository = webLinkRepository;
        this.skillRepository = skillRepository;
    }


    /**
     * Adds in the default evidence, skills and weblinks.
     */
    public void createEvidenceData() {
        try {
            LocalDate date = LocalDate.now();

            Evidence evidence = evidenceRepository.save(new Evidence(9, "Title", date, "Description"));
            Evidence evidence1 = evidenceRepository.save(new Evidence(9, "Created test Data", date, "Created a selection of default evidence objects for testing"));
            Evidence evidence2 = evidenceRepository.save(new Evidence(9, "making more evidence", date, "Description of another one"));
            Evidence evidence3 = evidenceRepository.save(new Evidence(9, "Writing Long Descriptions", date, "A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. "));
            Evidence evidence4 = evidenceRepository.save(new Evidence(9, "No Skill Evidence", date, "A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. "));

            WebLink webLink =  webLinkRepository.save(new WebLink(evidence, "localhost", "https://localhost"));
            WebLink webLink1 = webLinkRepository.save(new WebLink(evidence1,  "evidence1 weblink", "https://localhost/evidence1"));
            WebLink webLink2 = webLinkRepository.save(new WebLink(evidence1,  "lots of web links", "https://lotsOfTestWeblinks"));

            Skill skill = skillRepository.save(new Skill("test"));
            Skill skill1 = skillRepository.save(new Skill("java"));
            Skill skill2 = skillRepository.save(new Skill("debugging"));
            Skill skill3 = skillRepository.save(new Skill("making data"));

            evidence.addWebLink(webLink);
            evidence1.addWebLink(webLink1);
            evidence1.addWebLink(webLink2);

            evidence.addSkill(skill);
            evidence.addSkill(skill1);
            evidence.addSkill(skill2);
            evidence.addSkill(skill3);

            evidence1.addSkill(skill);
            evidence1.addSkill(skill1);
            evidence1.addSkill(skill2);

            evidence2.addSkill(skill2);
            evidence2.addSkill(skill3);

            evidence3.addSkill(skill);
            evidence3.addSkill(skill1);
            evidence3.addSkill(skill2);
            evidence3.addSkill(skill3);

            evidence.addCategory(Category.SERVICE);
            evidence.addCategory(Category.QUALITATIVE);
            evidence.addCategory(Category.QUANTITATIVE);
            evidence1.addCategory(Category.SERVICE);
            evidence1.addCategory(Category.QUALITATIVE);
            evidence2.addCategory(Category.SERVICE);
            evidence3.addCategory(Category.QUALITATIVE);
            evidence4.addCategory(Category.QUANTITATIVE);


            evidenceRepository.save(evidence);
            evidenceRepository.save(evidence1);
            evidenceRepository.save(evidence2);
            evidenceRepository.save(evidence3);
            evidenceRepository.save(evidence4);
        } catch (MalformedURLException exception) {
            logger.error("Error occurred loading default evidence");
            logger.error(exception.getMessage());
        }
    }
}
