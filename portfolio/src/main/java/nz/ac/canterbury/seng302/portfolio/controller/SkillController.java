package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.evidence.SkillRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for all the Skill based end points
 */
@Controller
public class SkillController {

    /** For logging the controller for debugging. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Holds persisted information about skills */
    @Autowired
    private SkillRepository skillRepository;

    /** For checking is a user exists and getting their details. */
    @Autowired
    private UserAccountsClientService userAccountsClientService;


    /**
     * Gets all the skills associated with a user with the supplied userId.
     *
     * @param userId - The userId of the user whose skills are requested
     * @return A ResponseEntity that contains a list of skills associated with the User.
     */
    @GetMapping("/skills")
    public ResponseEntity<Object> getSkillsByUserId(@RequestParam Integer userId) {
        logger.info("GET REQUEST /skills - attempt to get all skills for user: {}", userId);
        try {
            List<Skill> skills = skillRepository.findSkillsByEvidenceUserId(userId);
            if (skills.isEmpty()) {
                GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(userId).build();
                UserResponse userResponse = userAccountsClientService.getUserAccountById(request);
                if (userResponse.getId() == -1) {
                    logger.info("GET REQUEST /skills - user {} does not exist", userId);
                    return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
                }
            }
            logger.info("GET REQUEST /skills - found and returned {} skills for user: {}", skills.size() ,userId);
            return new ResponseEntity<>(skills, HttpStatus.OK);

        } catch (Exception exception) {
            logger.error("GET REQUEST /skills - Internal Server Error attempt user: {}", userId);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
