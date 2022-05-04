package nz.ac.canterbury.seng302.portfolio.projects.milestones;

import nz.ac.canterbury.seng302.portfolio.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;

import java.time.LocalDate;
import java.util.List;

public class MilestoneHelper {

    private MilestoneHelper() {}
    public static List<Milestone> setMilestoneColours(Long projectId, MilestoneRepository milestoneRepository, SprintRepository sprintRepository) {
        List<Milestone> milestoneList = milestoneRepository.findAllByProjectIdOrderByEndDate(projectId);
        List<Sprint> sprintList = sprintRepository.findAllByProjectId(projectId);
        for(Milestone milestone: milestoneList) {
            for (Sprint sprint: sprintList) {
                LocalDate eEnd = LocalDate.from(milestone.getEndDate());
                LocalDate sStart = sprint.getStartDate();
                LocalDate sEnd = sprint.getEndDate();
                if ((eEnd.isAfter(sStart) || eEnd.isEqual(sStart)) && (eEnd.isBefore(sEnd) || eEnd.isEqual(sEnd))){
                    //Event end date is between or equal to sprint start and end dates.
                    milestone.setEndDateColour(sprint.getColour());
                    if(!sprint.getMilestoneList().contains(milestone)) {
                        sprint.addMilestone(milestone);
                    }
                }
            }
        }
        return milestoneList;
    }
}
