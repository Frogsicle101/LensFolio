package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.DTO.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.evidence.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for all the Evidence based end points
 */
@Controller
public class EvidenceController {

    /** For logging the requests related to groups */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** For requesting user information form the IdP.*/
    private final UserAccountsClientService userAccountsClientService;

    /** The repository containing users pieces of evidence. */
    private final EvidenceRepository evidenceRepository;

    /** The repository containing web links. */
    private final WebLinkRepository webLinkRepository;

    /** The repository containing the projects. */
    private final ProjectRepository projectRepository;

    private final EvidenceService evidenceService;


    @Autowired
    public EvidenceController(UserAccountsClientService userAccountsClientService,
                           ProjectRepository projectRepository,
                           EvidenceRepository evidenceRepository,
                           WebLinkRepository webLinkRepository,
                           EvidenceService evidenceService) {
        this.userAccountsClientService = userAccountsClientService;
        this.projectRepository = projectRepository;
        this.evidenceRepository = evidenceRepository;
        this.webLinkRepository = webLinkRepository;
        this.evidenceService = evidenceService;
    }


    /**
     * Gets the evidence page for the logged-in user.
     *
     * @param principal The principal containing the logged-in user's Id.
     * @return A modelAndView object of the page.
     */
    @GetMapping("/evidence")
    public ModelAndView getEvidencePage(@AuthenticationPrincipal Authentication principal) {
        logger.info("GET REQUEST /evidence - attempt to get all groups");

        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);

        ModelAndView modelAndView = new ModelAndView("evidence");
        modelAndView.addObject("user", user);

        Project project = projectRepository.getProjectById(1L);
        LocalDate projectEndDate = project.getEndDate();
        LocalDate projectStartDate = project.getStartDate();
        LocalDate currentDate = LocalDate.now();
        LocalDate evidenceMaxDate = LocalDate.now();
        modelAndView.addObject("currentDate", currentDate.format(DateTimeFormat.yearMonthDay()));
        modelAndView.addObject("projectStartDate", projectStartDate.format(DateTimeFormat.yearMonthDay()));

        if (projectEndDate.isBefore(currentDate)) {
            evidenceMaxDate = projectEndDate;
        }
        modelAndView.addObject("evidenceMaxDate", evidenceMaxDate.format(DateTimeFormat.yearMonthDay()));

        return modelAndView;
    }


    /**
     * Gets the details for a piece of evidence with the given id
     *
     * Response codes: NOT_FOUND means the piece of evidence does not exist
     *                 OK means the evidence exists and an evidence details are returned.
     *                 BAD_REQUEST when the user doesn't interact with the endpoint correctly, i.e., no or invalid evidenceId
     *
     * @param evidenceId - The ID of the piece of evidence
     * @return A response entity with the required response code. Response body is the evidence is the status is OK
     */
    @GetMapping("/evidencePiece")
    public ResponseEntity<Object> getOneEvidence(@RequestParam("evidenceId") Integer evidenceId) {
        logger.info("GET REQUEST /evidence - attempt to get evidence with Id {}", evidenceId);
        try {
            Optional<Evidence> evidence = evidenceRepository.findById(evidenceId);
            if (evidence.isEmpty()) {
                logger.info("GET REQUEST /evidence - evidence {} does not exist", evidenceId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(evidence, HttpStatus.OK);
        } catch (Exception exception) {
            logger.warn(exception.getClass().getName());
            logger.warn(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Gets the details for a piece of evidence with the given id
     *
     * Response codes: NOT_FOUND means the piece of evidence does not exist
     *                 OK means the evidence exists and web link details are returned.
     *                 BAD_REQUEST when the user doesn't interact with the endpoint correctly, i.e., no or invalid evidenceId
     *
     * @param evidenceId - The ID of the piece of evidence
     * @return A response entity with the required response code. Response body is the evidence is the status is OK
     */
    @GetMapping("/evidencePieceWebLinks")
    public ResponseEntity<Object> getEvidenceWebLinks(@RequestParam("evidenceId") Integer evidenceId) {
        logger.info("GET REQUEST /evidencePieceWebLinks - attempt to get weblinks with evidence Id {}", evidenceId);
        try {
            Optional<Evidence> evidence = evidenceRepository.findById(evidenceId);
            if (evidence.isEmpty()) {
                logger.info("GET REQUEST /evidence - evidence {} does not exist", evidenceId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            List<WebLink> webLinks = evidence.get().getWebLinks();
            return new ResponseEntity<>(webLinks, HttpStatus.OK);
        } catch (Exception exception) {
            logger.warn(exception.getClass().getName());
            logger.warn(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Gets all the pieces of evidence for a requested user.
     *
     * Response codes: NOT_FOUND means the user does not exist
     * OK means the user exists and an evidence list is returned  (an empty list if no evidence exists)
     * BAD_REQUEST when the user doesn't interact with the endpoint correctly, i.e., no or invalid userId
     *
     * @param userId - The userId of the user whose evidence is wanted
     * @return A response entity with the required response code. Response body is the evidence is the status is OK
     */
    @GetMapping("/evidenceData")
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
     * Entrypoint for creating an evidence object.
     *
     * @param principal   The authentication principal for the logged-in user
     * @param evidenceDTO The EvidenceDTO object containing the required data for the evidence instance being created.

     * @return returns a ResponseEntity. This entity includes the new piece of evidence if successful.
     */
    @PostMapping(value = "/evidence")
    @ResponseBody
    public ResponseEntity<Object> addEvidence(
            @AuthenticationPrincipal Authentication principal,
            @RequestBody EvidenceDTO evidenceDTO
    ) {
        logger.info("POST REQUEST /evidence - attempt to create new evidence");
        try {
            Evidence evidence = evidenceService.addEvidence(principal, evidenceDTO);
            return new ResponseEntity<>(evidence, HttpStatus.OK);
        } catch (CheckException err) {
            logger.warn("POST REQUEST /evidence - attempt to create new evidence: Bad input: {}", err.getMessage());
            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DateTimeParseException err) {
            logger.warn("POST REQUEST /evidence - attempt to create new evidence: Bad date");
            return new ResponseEntity<>("Date is not in a parsable format", HttpStatus.BAD_REQUEST);
        } catch (MalformedURLException err) {
            logger.warn("POST REQUEST /evidence - attempt to create new evidence: Bad url {}", err.getMessage());
            return new ResponseEntity<>("Submitted weblink URL is malformed", HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("POST REQUEST /evidence - attempt to create new evidence: ERROR: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
