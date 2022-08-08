package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.evidence.*;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.Set;

/**
 * Controller for all the Category based end points
 */
@Controller
public class CategoryController {
    /** For logging the controller for debugging. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Holds persisted information about skills */
    @Autowired
    private SkillRepository skillRepository;

    /** Holds persisted information about category */
    @Autowired
    private CategoryRepository categoryRepository;

    /** For checking is a user exists and getting their details. */
    @Autowired
    private UserAccountsClientService userAccountsClientService;

    /**
     * Gets all the evidence associated with a user with the specified Category.
     *
     * @param categoryId - The Category id of the Category whose pieces of evidence are requested
     * @return A ResponseEntity that contains a list of evidences associated with the Category.
     */
    @GetMapping("/evidenceLinkedToCategory")
    public ResponseEntity<Object> getEvidenceByCategory(@RequestParam Integer categoryId) {
        logger.info("GET REQUEST /evidenceLinkedToCategory - attempt to get all evidence for category: {}", categoryId);
        try {
            Optional<Category> category = categoryRepository.findById(categoryId);
            if (category.isEmpty()) {
                logger.info("GET REQUEST /evidenceLinkedToCategory - category {} does not exist", categoryId);
                return new ResponseEntity<>("Category does not exist", HttpStatus.NOT_FOUND);
            }

            Set<Evidence> evidence = category.get().getEvidence();
            logger.info("GET REQUEST /evidenceLinkedToCategory - found and returned {} evidences for category: {}", evidence.size() ,categoryId);
            return new ResponseEntity<>(evidence, HttpStatus.OK);

        } catch (Exception exception) {
            logger.error("GET REQUEST /evidenceLinkedToCategory - Internal Server Error attempt category: {}", categoryId);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}