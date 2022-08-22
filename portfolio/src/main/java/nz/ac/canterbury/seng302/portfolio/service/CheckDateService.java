package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.SprintRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Checks if time values are contained in date ranges.
 */
@Service
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

    /**
     * Checks that the proposed new dates for the project don't fall inside existing sprints dates.
     * Also checks that the project's new date doesn't fall more than a year before the original start date.
     * @param sprintRepository The repository that stores the sprints.
     * @param project The project in question.
     * @param projectRequest The project request that contains all the proposed changes.
     */
    public void checkProjectAndItsSprintDates(SprintRepository sprintRepository, Project project, ProjectRequest projectRequest){
        List<Sprint> sprints = sprintRepository.findAllByProjectId(project.getId());
        sprints.sort((Comparator.comparing(Sprint::getStartDate)));
        LocalDate newProjectStart = LocalDate.parse(projectRequest.getProjectStartDate());
        LocalDate newProjectEnd = LocalDate.parse(projectRequest.getProjectEndDate());
        if (newProjectStart.isBefore(project.getStartDate().minusYears(1))) {
            throw new CheckException("Project cannot start more than a year before its original date");
        }
        if (newProjectStart.isAfter(newProjectEnd)){
            throw new CheckException("End date cannot be before start date");
        }
        if (!sprints.isEmpty()) {
            Sprint firstSprint = sprints.get(0);
            if (firstSprint.getEndDate().isAfter(newProjectEnd) || firstSprint.getStartDate().isAfter(newProjectEnd)) {
                throw new CheckException("There is a sprint that falls after these new dates");
            }
            Sprint lastSprint = sprints.get(sprints.size()-1);
            if (lastSprint.getStartDate().isBefore(newProjectStart) || lastSprint.getEndDate().isBefore(newProjectStart)) {
                throw new CheckException("There is a sprint that falls before these new dates");
            }
        }
    }


    /**
     * Checks that the project has room to add more sprints in.
     * Essentially checks that the new sprint start/end dates don't go past the end of the sprint.
     * @param sprintRepository the repository for sprints
     * @param project the project in question
     */
    public void checkProjectHasRoomForSprints(SprintRepository sprintRepository, Project project) {
        LocalDate startDate = project.getStartDate();
        List<Sprint> sprints = sprintRepository.findAllByProjectId(project.getId());
        sprints.sort((Comparator.comparing(Sprint::getStartDate)));
        if (!sprints.isEmpty()) {
            startDate = sprints.get(sprints.size()-1).getEndDate().plusDays(1);
        }
        if (startDate.isAfter(project.getEndDate())) {
            throw new CheckException("No more room to add sprints within project dates!");
        }
    }

    public void checkNewSprintDateNotInsideOtherSprints(LocalDate previousDateLimit, LocalDate nextDateLimit, SprintRequest sprintRequest){
        LocalDate startDate = LocalDate.parse(sprintRequest.getSprintStartDate());
        LocalDate endDate = LocalDate.parse(sprintRequest.getSprintEndDate());
        if (startDate.isBefore(previousDateLimit)) {
            throw new CheckException("Start date is before previous sprints end date / project start date");
        }
        if (endDate.isAfter(nextDateLimit)) {
            throw new CheckException("End date is after next sprints start date / project end date");
        }
    }


}
