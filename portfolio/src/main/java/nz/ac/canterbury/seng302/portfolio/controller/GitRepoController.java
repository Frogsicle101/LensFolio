package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.projects.repositories.GitRepoRepository;
import nz.ac.canterbury.seng302.portfolio.projects.repositories.GitRepository;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetGroupDetailsRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
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

import java.util.List;

/**
 * The controller for managing requests to edit git repositories and their settings.
 */
@Controller
public class GitRepoController {

    /** For logging the requests related to git repositories. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository in which group git repositories are stored. */
    @Autowired
    private GitRepoRepository gitRepoRepository;

    /** For making gRpc requests to the IdP. */
    @Autowired
    private GroupsClientService groupsClientService;


    /**
     * Mapping for a post request to add a git repository to a group. Restricted to group members,teachers, and admin.
     * The method checks that the given group Id is valid, and then creates a git repository object using the provided
     * group Id, project Id (the Id of the git project), git repository alias, and git repository access token. The
     * created repository is then saved to the git repository repository: the repository which stores git repositories.
     * The created git repository is then returned, with an OK message.
     *
     * If the group Id is not valid or the user is not a member of the group, an exception is thrown and an HTTP
     * response with a BAD REQUEST status is returned.
     *
     * @param groupId     The Id of the group to which the created git repository belongs.
     * @param projectId   The project Id of the git repository.
     * @param alias       The user-defined alias for the git repository.
     * @param accessToken The access token of the git repository.
     * @return A response entity indicating success or an error. On success, also return the created git repository.
     */
    @PostMapping("/editGitRepo")
    public ResponseEntity<Object> addGitRepo(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam Integer groupId,
            @RequestParam Integer projectId,
            @RequestParam String alias,
            @RequestParam String accessToken) {
        logger.info("POST REQUEST /gitRepo/add - attempt to add git repo {} to group {}", alias, groupId);

        try {
            if (alias.isBlank()) {
                throw new RuntimeException("The repository name cannot be empty");
            }
            if (accessToken.length() < 20) {
                throw new RuntimeException("Access token must be at least 20 characters");
            }


            GitRepository repo;
            try {
                repo = gitRepoRepository.findAllByGroupId(groupId).get(0);
                repo.setProjectId(projectId);
                repo.setAlias(alias);
                repo.setAccessToken(accessToken);
            } catch (IndexOutOfBoundsException e) {
                repo = new GitRepository(groupId, projectId, alias, accessToken);
            }

            gitRepoRepository.save(repo);
            logger.info("POST /gitRepo/add: Success");
            return new ResponseEntity<>(repo, HttpStatus.OK);

        } catch (RuntimeException exception) {
            logger.error("ERROR /gitRepo/add - an error occurred while adding git repo {} to group {}", alias, groupId);
            logger.error(exception.getMessage());
            return new ResponseEntity<>("Unable to edit the git repository information", HttpStatus.BAD_REQUEST);
        }
    }



    /**
     * Mapping for a get request to retrieving git repositories from a group by group ID.
     * The method checks that the given group Id is valid, and then search for the git repo by group ID.
     * The method does not change anything about git repository.
     *
     * If the group Id is not valid, an exception is thrown and an HTTP response with a BAD REQUEST status is returned.
     *
     * @param groupId     The Id of the group to which the created git repository belongs.
     * @return A response entity indicating success or an error. On success, also return the created git repository.
     */
    @GetMapping("/getRepo")
    public ResponseEntity<Object> getGitRepo(
            @RequestParam Integer groupId) {
        logger.info("GET REQUEST /getRepo - attempt to get git repo on group {}", groupId);
        try {
            //check groupId is correct
            GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                    .setGroupId(groupId)
                    .build();
            GroupDetailsResponse groupExistsConfirmation = groupsClientService.getGroupDetails(request);
            if (groupExistsConfirmation.getGroupId() == -1) {
                logger.warn("GET REQUEST /getRepo - group {} not found on the IDP", groupId);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            List<GitRepository> gitRepos = gitRepoRepository.findAllByGroupId(groupId);
            logger.info("GET /getRepo: Success");
            return new ResponseEntity<>(gitRepos, HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("GET REQUEST /getRepo - error occurred getting repo for group {}", groupId);
            logger.error(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

