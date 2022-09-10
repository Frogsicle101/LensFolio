package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.service.DateTimeService;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.service.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetPaginatedUsersFilteredRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.util.BasicStringFilteringOptions;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    /** The repository containing the projects. */
    private final ProjectRepository projectRepository;

    /** Provides helper functions for Crud operations on evidence */
    private final EvidenceService evidenceService;


    @Autowired
    public EvidenceController(UserAccountsClientService userAccountsClientService,
                           ProjectRepository projectRepository,
                           EvidenceRepository evidenceRepository,
                           EvidenceService evidenceService) {
        this.userAccountsClientService = userAccountsClientService;
        this.projectRepository = projectRepository;
        this.evidenceRepository = evidenceRepository;
        this.evidenceService = evidenceService;
    }


    /**
     * Gets the evidence page for the logged-in user.
     *
     * @param principal The principal containing the logged-in user's Id.
     * @return A modelAndView object of the page.
     */
    @GetMapping("/evidence")
    public ModelAndView getEvidenceBySkillsPage(@AuthenticationPrincipal Authentication principal) {
        logger.info("GET REQUEST /evidence/skills - attempt to get page");

        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);

        ModelAndView modelAndView = new ModelAndView("evidenceBySkills");
        modelAndView.addObject("user", user);

        Project project = projectRepository.getProjectById(1L);
        LocalDate projectEndDate = project.getEndDate();
        LocalDate projectStartDate = project.getStartDate();
        LocalDate currentDate = LocalDate.now();
        LocalDate evidenceMaxDate = LocalDate.now();
        modelAndView.addObject("currentDate", currentDate.format(DateTimeService.yearMonthDay()));
        modelAndView.addObject("projectStartDate", projectStartDate.format(DateTimeService.yearMonthDay()));

        if (projectEndDate.isBefore(currentDate)) {
            evidenceMaxDate = projectEndDate;
        }
        modelAndView.addObject("evidenceMaxDate", evidenceMaxDate.format(DateTimeService.yearMonthDay()));

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

            return new ResponseEntity<>(evidence.get(), HttpStatus.OK);
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
        logger.info("GET REQUEST /evidencePieceWebLinks - attempt to get web links with evidence Id {}", evidenceId);
        try {
            Optional<Evidence> evidence = evidenceRepository.findById(evidenceId);
            if (evidence.isEmpty()) {
                logger.info("GET REQUEST /evidence - evidence {} does not exist", evidenceId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Set<WebLink> webLinks = evidence.get().getWebLinks();
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
            List<Evidence> evidence = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(userId);
            GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(userId).build();
            UserResponse userResponse = userAccountsClientService.getUserAccountById(request);
            if (userResponse.getId() == -1) {
                logger.info("GET REQUEST /evidence - user {} does not exist", userId);
                return new ResponseEntity<>("Error: User not found", HttpStatus.NOT_FOUND);
            }
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Users-Name", userResponse.getFirstName() + ' ' + userResponse.getLastName());

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(evidence);
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
            return new ResponseEntity<>("Submitted web link URL is malformed", HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("POST REQUEST /evidence - attempt to create new evidence: ERROR: {}", err.getMessage());
            return new ResponseEntity<>("An unknown error occurred. Please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Deletes a piece of evidence owned by the user making the request.
     *
     * If the evidence is not owned by the user making the request, then the response is a 401,
     * If the evidence doesn't exist, then the response is a 404,
     * Any other issues return a 500 error,
     * Otherwise the response is OK,
     *
     * @param principal The user who made the request.
     * @param evidenceId The Id of the piece of evidence to be deleted.
     * @return ResponseEntity containing the HTTP status and a response message.
     */
    @DeleteMapping("/evidence")
    public ResponseEntity<Object> deleteEvidence(@AuthenticationPrincipal Authentication principal,
                                                 @RequestParam Integer evidenceId) {
        String methodLoggingTemplate = "DELETE /evidence: {}";
        logger.info(methodLoggingTemplate, "Called");
        try {
            Optional<Evidence> optionalEvidence = evidenceRepository.findById(evidenceId);
            if (optionalEvidence.isEmpty()) {
                String message = "No evidence found with id " + evidenceId;
                logger.info(methodLoggingTemplate, message);
                return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
            }
            Evidence evidence = optionalEvidence.get();
            int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
            if (evidence.getUserId() != userId) {
                logger.warn(methodLoggingTemplate, "User attempted to delete evidence they don't own.");
                return new ResponseEntity<>("You can only delete evidence that you own.", HttpStatus.UNAUTHORIZED);
            }
            evidenceRepository.delete(evidence);
            String message = "Successfully deleted evidence " + evidenceId;
            logger.info(methodLoggingTemplate, message);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception exception) {
            logger.error(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>("An unexpected error has occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Checks if the provided web address is valid, i.e. could lead to a website.
     * The criteria are specified by java.net.URL, and is protocol dependent.
     * This doesn't guarantee that the website actually exists; just that it could.
     *
     * Response codes:
     * OK means the address is valid
     * BAD_REQUEST means the URL is invalid
     * INTERNAL_SERVER_ERROR means some other error occurred while validating the URL
     *
     * @param request - the full address to be validated
     * @return A response entity with the required response code. If it is valid, the status will be OK.
     * No response body will be returned in any instance.
     * @see java.net.URL
     */
    @PostMapping("/validateWebLink")
    @ResponseBody
    public ResponseEntity<Object> validateWebLink(@RequestBody WebLinkDTO request) {
        String address = request.getUrl();
        logger.info("GET REQUEST /validateWebLink - validating address {}", address);
        try {
            if (address.contains("&nbsp")) {
                throw new MalformedURLException("The non-breaking space is not a valid character");
            }
            new URL(address).toURI(); //The constructor does all the validation for us
            //If you want to ban a webLink URL, like, say, the original rick roll link, the code would go here.
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MalformedURLException | URISyntaxException exception) {
            logger.warn("/validateWebLink - invalid address {}", address);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            logger.warn(exception.getClass().getName());
            logger.warn(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * A get request for retrieving the users that match the string being typed
     *
     * @param name The name that is being typed by the user
     * @return A ResponseEntity. This entity includes the filtered users if successful, otherwise an error message
     */
    @GetMapping("/filteredUsers")
    public ResponseEntity<Object> getFilteredUsers(@RequestParam("name") String name){
        try {
            logger.info("GET REQUEST /filteredUsers - retrieving filtered users with string {}", name);
            PaginationRequestOptions options = PaginationRequestOptions.newBuilder()
                    .setOffset(0)
                    .setLimit(999999999) // we want to retrieve all users
                    .setOrderBy("name")
                    .setIsAscendingOrder(true)
                    .build();
            BasicStringFilteringOptions filter = BasicStringFilteringOptions.newBuilder()
                    .setFilterText(name)
                    .build();
            GetPaginatedUsersFilteredRequest request = GetPaginatedUsersFilteredRequest.newBuilder()
                    .setPaginationRequestOptions(options)
                    .setFilteringOptions(filter)
                    .build();
            PaginatedUsersResponse response = userAccountsClientService.getPaginatedUsersFilteredByName(request);
            return new ResponseEntity<>(response.getUsersList(), HttpStatus.OK);
        } catch (Exception e){
            logger.warn(e.getClass().getName());
            logger.warn(e.getMessage());
            return new ResponseEntity<>("An unknown error occurred. Please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
