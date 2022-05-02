package nz.ac.canterbury.seng302.portfolio.projects.deadlines;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;

import java.time.LocalDate;
import java.time.LocalTime;

public class Deadline extends Milestone {

    private LocalTime endTime;

    public Deadline() {
    }

    public Deadline(Project project, String name, LocalDate endDate, LocalTime endTime) {
        super(project, name, endDate);
        this.endTime = endTime;
    }
}
