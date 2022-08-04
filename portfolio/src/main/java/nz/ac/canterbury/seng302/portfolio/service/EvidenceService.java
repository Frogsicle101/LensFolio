package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.evidence.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to differentiate the strings that are passed to the stringCheck method
 */
enum StringType {
    TITLE,
    DESCRIPTION,
}

/**
 * A utility class for more complex actions involving Evidence
 */
@Service
public class EvidenceService {

    static Pattern alpha = Pattern.compile("[a-zA-Z]");

    private final UserAccountsClientService userAccountsClientService;

    private final ProjectRepository projectRepository;

    private final EvidenceRepository evidenceRepository;

    @Autowired
    public EvidenceService(UserAccountsClientService userAccountsClientService,
                           ProjectRepository projectRepository,
                           EvidenceRepository evidenceRepository) {
        this.userAccountsClientService = userAccountsClientService;
        this.projectRepository = projectRepository;
        this.evidenceRepository = evidenceRepository;
    }


    /**
     * Checks if the string is too short or matches the pattern provided
     * if either of these are true then it throws an exception
     *
     * @param string A string
     * @throws CheckException The exception to throw
     */
    public void checkString(String string, StringType type) throws CheckException {
        Matcher matcher = alpha.matcher(string);

        if (string.length() < 2) {
            throw new CheckException("Text should be longer than 1 character");
        } else if (!matcher.find()) {
            throw new CheckException("Text shouldn't be strange");
        }

        if (type == StringType.TITLE && string.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        } else if (type == StringType.DESCRIPTION && string.length() > 500){
            throw new CheckException("Description cannot be more than 500 characters");
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

    /**
     * Creates a new evidence object and saves it to the repository. Adds any weblink objects to the evidence object
     * before saving it if needed.
     *
     * @param principal   The authentication principal
     * @param title       The title of the evidence
     * @param date        The date of the evidence
     * @param description The description of the evidence
     * @param projectId   The project id
     * @param webLinks    A list of weblinkDTOs to be added to the evidence
     * @return The evidence object, after it has been added to the database.
     * @throws MalformedURLException When one of the weblinks has a malformed url
     */
    public Evidence addEvidence(Authentication principal,
                                String title,
                                String date,
                                String description,
                                long projectId,
                                List<WebLinkDTO> webLinks) throws MalformedURLException {
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isEmpty()) {
            throw new CheckException("Project Id does not match any project");
        }
        Project project = optionalProject.get();
        LocalDate localDate = LocalDate.parse(date);
        checkDate(project, localDate);

        checkString(title, StringType.TITLE);
        checkString(description, StringType.DESCRIPTION);

        Evidence evidence = new Evidence(user.getId(), title, localDate, description);

        if (webLinks != null) {
            for (WebLinkDTO dto : webLinks) {
                WebLink webLink = new WebLink(evidence, dto.getName(), dto.getUrl());
                evidence.addWebLink(webLink);
            }
        }

        return evidenceRepository.save(evidence);
    }
}
