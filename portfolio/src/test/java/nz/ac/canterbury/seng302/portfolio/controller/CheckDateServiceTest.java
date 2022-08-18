package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.CheckDateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckDateServiceTest {

    Project project;
    SprintRepository mockSprintRepository = mock(SprintRepository.class);
    List<Sprint> sprintList = new ArrayList<>();
    CheckDateService checkDateService;

    @BeforeEach
    void setup() {
        project = new Project("test");
        project.setStartDate(LocalDate.parse("1500-01-01"));
        project.setEndDate(LocalDate.parse("1999-01-01"));
        Sprint sprint = new Sprint(project, "test", LocalDate.parse("1799-01-01"), LocalDate.parse("1801-01-01"));
        Sprint sprint2 = new Sprint(project, "test", LocalDate.parse("1900-01-01"), LocalDate.parse("1900-01-04"));
        sprintList.add(sprint);
        sprintList.add(sprint2);
        when(mockSprintRepository.getAllByProjectOrderByStartDateAsc(project)).thenReturn(sprintList);
        checkDateService = new CheckDateService();
    }


    @Test
    void testDateOutsideSprints() {
        LocalDate date = LocalDate.parse("1700-01-01");
        Assertions.assertFalse(checkDateService.dateIsInSprint(date, project, mockSprintRepository));
    }


    @Test
    void testDateInSprints() {
        LocalDate date = LocalDate.parse("1800-01-01");
        Assertions.assertTrue(checkDateService.dateIsInSprint(date, project, mockSprintRepository));
    }


    @Test
    void testDateOnSprintBoundaryStart() {
        LocalDate date = LocalDate.parse("1900-01-01");
        Assertions.assertTrue(checkDateService.dateIsInSprint(date, project, mockSprintRepository));
    }


    @Test
    void testDateOnSprintBoundaryEnd() {
        LocalDate date = LocalDate.parse("1900-01-04");
        Assertions.assertTrue(checkDateService.dateIsInSprint(date, project, mockSprintRepository));
    }


    @Test
    void testDateOutsideProject() {
        LocalDate date = LocalDate.parse("2000-01-01");
        Assertions.assertFalse(checkDateService.dateIsInSprint(date, project, mockSprintRepository));
    }
}
