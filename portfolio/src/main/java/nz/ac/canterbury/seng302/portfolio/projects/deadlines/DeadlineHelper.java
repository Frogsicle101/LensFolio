package nz.ac.canterbury.seng302.portfolio.projects.deadlines;

import nz.ac.canterbury.seng302.portfolio.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;

import java.time.LocalDate;
import java.util.List;

public class DeadlineHelper {

    private DeadlineHelper() {
    }

    public static List<Deadline> setDeadlineColours(Long projectId, DeadlineRepository deadlineRepository, SprintRepository sprintRepository) {

        List<Deadline> deadlineList = deadlineRepository.findAllByProjectId(projectId);
        List<Sprint> sprintList = sprintRepository.findAllByProjectId(projectId);
        for(Deadline deadline: deadlineList) {
            for (Sprint sprint: sprintList) {
                LocalDate eEnd = LocalDate.from(deadline.getEndDate());
                LocalDate sStart = sprint.getStartDate();
                LocalDate sEnd = sprint.getEndDate();
                if ((eEnd.isAfter(sStart) || eEnd.isEqual(sStart)) && (eEnd.isBefore(sEnd) || eEnd.isEqual(sEnd))){
                    //Event end date is between or equal to sprint start and end dates.
                    deadline.setEndDateColour(sprint.getColour());
                    if(!sprint.getDeadlineList().contains(deadline)) {
                        sprint.addDeadline(deadline);
                    }
                }
            }
        }
        return deadlineList;
    }
}
