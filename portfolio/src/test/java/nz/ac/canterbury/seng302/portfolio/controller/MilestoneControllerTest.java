package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.InvalidNameException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;

public class MilestoneControllerTest {

    private final ProjectRepository mockProjectRepository = mock(ProjectRepository.class);
    private final MilestoneRepository mockMilestoneRepository = mock(MilestoneRepository.class);

    private final AuthState principal = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();
    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);


    private MilestoneController milestoneController = new MilestoneController(mockProjectRepository, mockMilestoneRepository);

    private Project project =  new Project("test");;


    @BeforeEach
    public void beforeEach() {

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.TEACHER);
        UserResponse user = userBuilder.build();


        Mockito.when(PrincipalAttributes.getUserFromPrincipal(principal, mockClientService)).thenReturn(user);
        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        Mockito.when(mockClientService.getUserAccountById(userByIdRequest)).thenReturn(user);
        Mockito.when(mockProjectRepository.findById(Mockito.any())).thenReturn(Optional.of(project));

    }

    @Test
    void testAddMilestone() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), milestone.getEndDate().toString(), milestone.getType());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void testAddMilestoneNoProject() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockProjectRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), milestone.getEndDate().toString(), milestone.getType());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }


    @Test
    void testAddMilestoneBadTitle() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "@", LocalDate.now(), 1);
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), milestone.getEndDate().toString(), milestone.getType());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Name does not match required pattern", response.getBody());
    }

    @Test
    void testAddMilestoneBadDate() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), LocalDate.now().minusYears(1).toString(), milestone.getType());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("End date must occur during project", response.getBody());
    }


    @Test
    void testAddMilestoneBadDateCantParse() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), "cheese", milestone.getType());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Could not parse date(s)", response.getBody());
    }





    @Test
    void testEditMilestone() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "Name", String.valueOf(LocalDate.now().plusDays(1)), 2);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testEditMilestoneNoMilestone() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "Name", String.valueOf(LocalDate.now().plusDays(1)), 2);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testEditMilestoneBadParse() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "Name", "cheese", 2);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Could not parse date(s)", response.getBody());
    }

    @Test
    void testEditMilestoneBadDate() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "Name", String.valueOf(LocalDate.now().minusYears(1)), 2);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("End date must occur during project", response.getBody());
    }


    @Test
    void testGetMilestoneList() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Milestone milestone2 = new Milestone(project, "testMilestone", LocalDate.now().plusDays(3), 1);
        Milestone milestone3 = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        List<Milestone> milestoneList = new ArrayList<>();
        milestoneList.add(milestone);
        milestoneList.add(milestone2);
        milestoneList.add(milestone3);

        Mockito.when(mockMilestoneRepository.findAllByProjectIdOrderByEndDate(Mockito.any())).thenReturn(milestoneList);
        ResponseEntity<Object> response = milestoneController.getMilestonesList(project.getId());
        List<Milestone> milestoneListReturned = (List<Milestone>) response.getBody();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(milestoneListReturned.contains(milestone));
        Assertions.assertTrue(milestoneListReturned.contains(milestone2));
        Assertions.assertTrue(milestoneListReturned.contains(milestone3));
        Assertions.assertEquals(milestoneListReturned.get(0), milestone3);

    }


    @Test
    void testGetMilestone() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.getMilestone(milestone.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(milestone, response.getBody());
    }

    @Test
    void testGetMilestoneNotFound() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<Object> response = milestoneController.getMilestone(milestone.getId());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void testDeleteMilestone() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.deleteMilestone(milestone.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void testDeleteMilestoneNotFound() throws InvalidNameException {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<Object> response = milestoneController.deleteMilestone(milestone.getId());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }






}



