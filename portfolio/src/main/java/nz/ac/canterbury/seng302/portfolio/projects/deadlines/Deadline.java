package nz.ac.canterbury.seng302.portfolio.projects.deadlines;

import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Inheritance
public class Deadline extends Milestone {

    private LocalTime endTime;

    public Deadline() {
    }

    public Deadline(Project project, String name, LocalDate endDate, LocalTime endTime) {
        super(project, name, endDate);
        this.endTime = endTime;
        assert(this.endTime != null);
    }

    @Override
    public String getEndDateFormatted() {
        return LocalDateTime.of(getEndDate(), this.endTime).format(DateTimeFormat.timeDateMonthYear());
    }
}
