package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.InvalidNameException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeadlineControllerTest {

    private final DeadlineRepository deadlineRepository = new DeadlineRepository() {
        @Override
        public List<Deadline> findAllByProjectId(Long projectId) {
            List<Deadline> deadlineList = new ArrayList<>();
            for (Deadline deadline : deadlines) {
                if (deadline.getProject().getId() == projectId) {
                    deadlineList.add(deadline);
                }
            }
            return deadlineList;
        }

        @Override
        public Long countDeadlineByProjectId(Long projectId) {
            return (long) deadlines.size();
        }

        @Override
        public List<Deadline> findAllByProjectIdOrderByEndDate(Long projectId) {
            return null;
        }

        @Override
        public Deadline getById(String eventId) {
            return null;
        }

        @Override
        public <S extends Deadline> S save(S entity) {
            if (!deadlines.contains(entity)) {
                entity.setUuid(UUID.randomUUID().toString());
                deadlines.add(entity);
            }
            return entity;
        }

        @Override
        public <S extends Deadline> Iterable<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public Optional<Deadline> findById(String uuid) {
            for (Deadline deadline : deadlines) {
                if (deadline.getId() == uuid) {
                    return Optional.of(deadline);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean existsById(String uuid) {
            return false;
        }

        @Override
        public Iterable<Deadline> findAll() {
            return null;
        }

        @Override
        public Iterable<Deadline> findAllById(Iterable<String> uuids) {
            return null;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(String uuid) {

        }

        @Override
        public void delete(Deadline entity) {
            deadlines.remove(entity);
        }

        @Override
        public void deleteAllById(Iterable<? extends String> uuids) {

        }

        @Override
        public void deleteAll(Iterable<? extends Deadline> entities) {

        }

        @Override
        public void deleteAll() {

        }
    };

    private static final ProjectRepository mockProjectRepository = mock(ProjectRepository.class);
    private static final PrincipalAttributes mockPrincipal = mock(PrincipalAttributes.class);
    private static final UserAccountsClientService clientService = mock(UserAccountsClientService.class);
    private final Authentication principal = new Authentication(AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build());

    private final DeadlineController deadlineController = new DeadlineController(mockProjectRepository, deadlineRepository);
    private final ArrayList<Deadline> deadlines = new ArrayList<>();
    private static Project project;

    @BeforeAll
    public static void beforeAll() {
        project = new Project("default", LocalDate.parse("2022-01-01"), LocalDate.parse("2022-12-31"), "test");
        when(mockProjectRepository.findById(project.getId())).thenReturn(java.util.Optional.ofNullable(project));
    }

    /**
     * Used to create an unauthorised user and to create the mock response for the Principal Attributes
     */
    public void createUnauthorisedUser() {
        deadlineController.setUserAccountsClientService(clientService);
        UserResponse.Builder user = UserResponse.newBuilder();
        user.setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        user.addRoles(UserRole.STUDENT);

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), clientService)).thenReturn(user.build());
    }

    private void createAuthorisedUser() {
        deadlineController.setUserAccountsClientService(clientService);
        UserResponse.Builder user = UserResponse.newBuilder();
        user.setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        user.addRoles(UserRole.TEACHER);

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), clientService)).thenReturn(user.build());
    }

    // These tests are for the create method

    @Test
    void createDeadlineUserNotAuthorisedTest() {
        createUnauthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, null, null, null, 1);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineInvalidProjectId() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, 2L, null, null, 1);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineValidName() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), "Deadline Name", null, 1);
        String expectedName = "Deadline Name";
        Assertions.assertEquals(expectedName, deadlines.get(0).getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createDeadlineNameLongerThan50Characters() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), "This is fifty-one characters, which is more than 50", null, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineNoNameNoOtherDeadlines() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, null, 1);
        String expectedName = "Deadline 1";
        Assertions.assertEquals(expectedName, deadlines.get(0).getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createDeadlineNoNameOneOtherDeadline() {
        createAuthorisedUser();
        deadlineController.addDeadline(principal, project.getId(), "A Deadline", null, 1);
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, null, 1);
        String expectedName = "Deadline 2";
        Assertions.assertEquals(expectedName, deadlines.get(1).getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createDeadlineValidDate() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, "2022-06-14T00:00:00.00", 1);
        LocalDate expectedDate = LocalDate.parse("2022-06-14");
        Assertions.assertEquals(expectedDate, deadlines.get(0).getEndDate());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createDeadlineDateBeforeProjectStartDate() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, "2021-01-01T00:00:00.00", 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineDateAfterProjectEndDate() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, "2023-01-01T00:00:00.00", 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineNoDateTodayBeforeProjectStart() {
        createAuthorisedUser();
        project = new Project("default", LocalDate.parse("2040-01-01"), LocalDate.parse("2040-12-31"), "test");
        when(mockProjectRepository.findById(project.getId())).thenReturn(java.util.Optional.ofNullable(project));
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, null, 1);
        LocalDate expectedDate = LocalDate.parse("2040-01-01");
        Assertions.assertEquals(expectedDate, deadlines.get(0).getEndDate());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createDeadlineNoDateTodayAfterProjectStart() {
        createAuthorisedUser();
        project = new Project("default", LocalDate.parse("2022-01-01"), LocalDate.parse("2040-12-31"), "test");
        when(mockProjectRepository.findById(project.getId())).thenReturn(java.util.Optional.ofNullable(project));
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, null, 1);
        LocalDate expectedDate = LocalDate.now();
        Assertions.assertEquals(expectedDate, deadlines.get(0).getEndDate());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createDeadlineInvalidDateString() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, "INVALID", 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineValidTypeOfOccasion() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, null, 2);
        Assertions.assertEquals(2, deadlines.get(0).getType());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createDeadlineInvalidTypeOfOccasion() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.addDeadline(principal, project.getId(), null, null, 0);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    // These tests are for the edit method

    @Test
    void EditDeadlineUserNotAuthorisedTest() {
        createUnauthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), "Shouldn't change", null, null, 1);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals("ToBeEdited", deadlines.get(0).getName());
    }

    @Test
    void editDeadlineInvalidDeadlineId() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.editDeadline(principal, UUID.randomUUID().toString(), project.getId(), null, null, null, 1);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void editDeadlineInvalidProjectId() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), 1L, "Shouldn't change", null, null, 1);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals("ToBeEdited", deadlines.get(0).getName());
    }

    @Test
    void editDeadlineValidName() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), "NewName", null, null, 1);
        Assertions.assertEquals("NewName", deadlines.get(0).getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineNameLongerThan50Characters() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), "This is fifty-one characters, which is more than 50", null, null, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("ToBeEdited", deadlines.get(0).getName());
    }

    @Test
    void editDeadlineNoName() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, null, null, 1);
        Assertions.assertEquals("ToBeEdited", deadlines.get(0).getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineValidDate() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, "2022-06-18", null, 1);
        LocalDate expectedDate = LocalDate.parse("2022-06-18");
        Assertions.assertEquals(expectedDate, deadlines.get(0).getEndDate());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineDateBeforeProjectStartDate() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, "2021-01-01", null, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(project.getStartDate(), deadlines.get(0).getEndDate());
    }

    @Test
    void editDeadlineDateAfterProjectEndDate() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, "2023-01-01", null, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(project.getStartDate(), deadlines.get(0).getEndDate());
    }

    @Test
    void editDeadlineNoDate() {
        createAuthorisedUser();
        project = new Project("default", LocalDate.parse("2022-01-01"), LocalDate.parse("2022-12-31"), "test");
        when(mockProjectRepository.findById(project.getId())).thenReturn(java.util.Optional.ofNullable(project));
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", LocalDate.parse("2022-06-21"), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, null, null, 1);
        LocalDate expectedDate = LocalDate.parse("2022-06-21");
        Assertions.assertEquals(expectedDate, deadlines.get(0).getEndDate());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineInvalidDateString() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, "INVALID", null, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(project.getStartDate(), deadlines.get(0).getEndDate());
    }

    @Test
    void editDeadlineValidTime() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, null, "12:30:22", 1);
        LocalTime expectedTime = LocalTime.parse("12:30:22");
        Assertions.assertEquals(expectedTime, deadlines.get(0).getEndTime());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineInvalidTimeString() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.parse("12:30:21"), 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, "INVALID", null, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(LocalTime.parse("12:30:21"), deadlines.get(0).getEndTime());
    }

    @Test
    void editDeadlineNoTime() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.parse("12:30:21"), 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, null, null, 1);
        LocalTime expectedTime = LocalTime.parse("12:30:21");
        Assertions.assertEquals(expectedTime, deadlines.get(0).getEndTime());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineValidTypeOfOccasion() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, null, null, 2);
        Assertions.assertEquals(2, deadlines.get(0).getType());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineInvalidTypeOfOccasion() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.editDeadline(principal, deadline.getId(), project.getId(), null, null, null, 0);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(1, deadlines.get(0).getType());
    }

    @Test
    void editDeadlineMultipleDeadlinesSaved() {
        createAuthorisedUser();
        Deadline deadline1 = null;
        try {
            deadline1 = new Deadline(project, "ToStayTheSame", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline1);

        Deadline deadline2 = null;
        try {
            deadline2 = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline2);

        ResponseEntity response = deadlineController.editDeadline(principal, deadline2.getId(), project.getId(), "NewName", null, null, null);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("NewName", deadlines.get(1).getName());
        Assertions.assertEquals("ToStayTheSame", deadlines.get(0).getName());
    }

    // These tests are for the delete method

    @Test
    void deleteDeadlineNotAuthenticated() {
        createUnauthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeDeleted", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.deleteDeadline(principal, deadline.getId());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Assertions.assertEquals(1, deadlines.size());
    }

    @Test
    void deleteDeadlineInvalidDeadlineId() {
        createAuthorisedUser();
        ResponseEntity response = deadlineController.editDeadline(principal, UUID.randomUUID().toString(), project.getId(), null, null, null, 1);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteDeadlineValidDeadlineId() {
        createAuthorisedUser();
        Deadline deadline = null;
        try {
            deadline = new Deadline(project, "ToBeDeleted", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline);
        ResponseEntity response = deadlineController.deleteDeadline(principal, deadline.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    // These tests are for the get one deadline method

    @Test
    void getDeadlineInvalidId() {
        createAuthorisedUser();
        Deadline deadline1 = null;
        try {
            deadline1 = new Deadline(project, "aDeadline", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline1);

        Deadline deadline2 = null;
        try {
            deadline2 = new Deadline(project, "aDeadline2", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline2);
        String invalidId = UUID.randomUUID().toString();
        //used to ensure it is actually invalid
        while (Objects.equals(invalidId, deadline1.getId()) || Objects.equals(invalidId, deadline2.getId())) {
            invalidId = UUID.randomUUID().toString();
        }

        ResponseEntity response = deadlineController.getDeadline(invalidId);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(null, response.getBody());
    }

    @Test
    void getDeadlineValidId() {
        createAuthorisedUser();
        Deadline deadline1 = null;
        try {
            deadline1 = new Deadline(project, "aDeadline", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline1);

        Deadline deadline2 = null;
        try {
            deadline2 = new Deadline(project, "aDeadline2", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline2);

        ResponseEntity response = deadlineController.getDeadline(deadline2.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("aDeadline2", ((Deadline) response.getBody()).getName());
    }

    // These tests are for the get list of deadlines method

    @Test
    void getDeadlinesListInvalidId() {
        createAuthorisedUser();

        Deadline deadline1 = null;
        try {
            deadline1 = new Deadline(project, "aDeadline", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline1);

        Deadline deadline2 = null;
        try {
            deadline2 = new Deadline(project, "aDeadline2", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline2);

        Long invalidId = project.getId() + 1;
        ResponseEntity response = deadlineController.getDeadlinesList(invalidId);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(0, ((List<Deadline>) response.getBody()).size());
    }

    @Test
    void getDeadlinesListValidId() {
        createAuthorisedUser();

        Deadline deadline1 = null;
        try {
            deadline1 = new Deadline(project, "aDeadline", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline1);

        Deadline deadline2 = null;
        try {
            deadline2 = new Deadline(project, "aDeadline2", project.getStartDate(), LocalTime.MIN, 1);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        deadlineRepository.save(deadline2);

        Long invalidId = project.getId();
        ResponseEntity response = deadlineController.getDeadlinesList(invalidId);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(2, ((List<Deadline>) response.getBody()).size());
    }

}
