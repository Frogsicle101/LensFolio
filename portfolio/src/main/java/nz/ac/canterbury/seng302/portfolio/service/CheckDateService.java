package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;

import java.time.LocalDate;

/**
 * Checks if time values are contained in date ranges.
 */
public class CheckDateService {

    /**
     * Checks if the given date occurs during a sprint in the given project.
     *
     * @param dateToCheck      The date to be checked.
     * @param project          The project containing the sprints being used as date ranges.
     * @param sprintRepository The repository containing sprints which the date is checked against.
     * @return Whether the date is contained in any of the sprints for the given project.
     */
    public boolean dateIsInSprint(LocalDate dateToCheck, Project project, SprintRepository sprintRepository) {
        boolean isInSprint = false;
        for (Sprint sprint : sprintRepository.getAllByProjectOrderByStartDateAsc(project)) {
            if ((sprint.getStartDate().minusDays(1L).isBefore(dateToCheck)) && sprint.getEndDate().plusDays(1L).isAfter(dateToCheck)) {
                isInSprint = true;
                return isInSprint;
            }
        }
        return isInSprint;
    }
}
