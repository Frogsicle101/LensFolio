package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLinkRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.MalformedURLException;
import java.time.LocalDate;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WebLinkTest {

    @Autowired
    EvidenceRepository evidenceRepository;

    @Autowired
    WebLinkRepository webLinkRepository;


    @Test
    void createTestEvidenceSingleWebLink() throws MalformedURLException {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        WebLink webLink = new WebLink(evidence, "Test", "https://www.google.co.nz");
        evidence.addWebLink(webLink);
        evidenceRepository.save(evidence);
        webLinkRepository.save(webLink);

        Evidence evidence1 = evidenceRepository.findAllByUserIdOrderByDateDesc(1).get(0);
        Assertions.assertEquals(evidence1.getTitle(), evidence.getTitle());
        Assertions.assertEquals(evidence1.getWebLinks().iterator().next().getUrl(), evidence.getWebLinks().iterator().next().getUrl());

    }


    @Test
    void createTestEvidenceMultipleWebLinks() throws MalformedURLException {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        WebLink webLink = new WebLink(evidence, "Test", "https://www.google.co.nz");
        WebLink webLink2 = new WebLink(evidence, "Test", "https://www.google.co.nz");
        WebLink webLink3 = new WebLink(evidence, "Test", "https://www.google.co.nz");
        evidence.addWebLink(webLink);
        evidence.addWebLink(webLink2);
        evidence.addWebLink(webLink3);
        evidenceRepository.save(evidence);
        webLinkRepository.save(webLink);
        webLinkRepository.save(webLink2);
        webLinkRepository.save(webLink3);

        Evidence evidence1 = evidenceRepository.findAllByUserIdOrderByDateDesc(1).get(0);
        Assertions.assertEquals(evidence1.getTitle(), evidence.getTitle());
        Assertions.assertEquals(evidence1.getWebLinks().iterator().next().getUrl(), evidence.getWebLinks().iterator().next().getUrl());
        Assertions.assertEquals(evidence1.getWebLinks().size(), evidence.getWebLinks().size());

    }

}
