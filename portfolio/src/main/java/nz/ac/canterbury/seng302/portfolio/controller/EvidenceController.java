package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Controller for all the Evidence based end points
 */
@Controller
public class EvidenceController {

    /** For logging the requests related to groups */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** For requesting user information form the IdP.*/
    @Autowired
    private UserAccountsClientService userAccountsClientService;

    /** The repository containing users pieces of evidence. */
    @Autowired
    private EvidenceRepository evidenceRepository;

    /** The repository containing the projects. */
    @Autowired
    private ProjectRepository projectRepository;


    /**
     * Gets all the pieces of evidence for a requested user.
     *
     * Response codes: NOT_FOUND means the user does not exist
     *                 OK means the user exists and an evidence list is returned  (an empty list if no evidence exists)
     *                 BAD_REQUEST when the user doesnt interact with the endpoint correctly, i.e., no or invalid userId
     *
     * @param userId - The userId of the user whose evidence is wanted
     * @return A response entity with the required response code. Response body is the evidence is the status is OK
     */
    @GetMapping("/evidence")
    public ResponseEntity<Object> getAllEvidence(@RequestParam("userId") Integer userId) {
        logger.info("GET REQUEST /evidence - attempt to get evidence for user {}", userId);
        try {
            List<Evidence> evidence = evidenceRepository.findAllByUserIdOrderByDateDesc(userId);
            if (evidence.isEmpty()) {
                GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(userId).build();
                UserResponse userExistsResponse = userAccountsClientService.getUserAccountById(request);
                if (userExistsResponse.getId() == -1) {
                    logger.info("GET REQUEST /evidence - user {} does not exist", userId);
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            }
            return new ResponseEntity<>(evidence, HttpStatus.OK);
        } catch (Exception exception) {
            logger.warn(exception.getClass().getName());
            logger.warn(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Entrypoint for creating an evidence object
     *
     * @param principal The authentication principal
     * @param title The title of the evidence
     * @param date The date of the evidence
     * @param description The description of the evidence
     * @param projectId The project id
     * @return returns a ResponseEntity
     */
    @PostMapping("/evidence")
    public ResponseEntity<Object> addEvidence(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam String title,
            @RequestParam String date,
            @RequestParam String description,
            @RequestParam long projectId
    ) {
        logger.info("POST REQUEST /evidence - attempt to create new evidence");
        try{
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
            Optional<Project> optionalProject = projectRepository.findById(projectId);
            if (optionalProject.isEmpty()) {
                throw new CheckException("Project Id does not match any project");
            }
            Project project = optionalProject.get();
            LocalDateTime localDateTime = LocalDateTime.parse(date);
            if (localDateTime.isBefore(project.getStartDateAsLocalDateTime())
                    || localDateTime.isAfter(project.getEndDateAsLocalDateTime())) {
                return new ResponseEntity<>("Date is outside project dates", HttpStatus.BAD_REQUEST);
            }
            EvidenceService.checkString(title);
            EvidenceService.checkString(description);
            Evidence evidence = new Evidence(user.getId(), title, localDateTime, description);
            evidenceRepository.save(evidence);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(CheckException err) {
            logger.warn("POST REQUEST /evidence - attempt to create new evidence: Bad input: {}", err.getMessage());
            return new ResponseEntity<>( err.getMessage(), HttpStatus.BAD_REQUEST);
        } catch(DateTimeParseException err) {
            logger.warn("POST REQUEST /evidence - attempt to create new evidence: Bad date: {}", date);
            return new ResponseEntity<>( "Date is not in a parsable format", HttpStatus.BAD_REQUEST);
        } catch(Exception err) {
            logger.error("POST REQUEST /evidence - attempt to create new evidence: ERROR: {}", err.getMessage());
            return new ResponseEntity<>(  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}