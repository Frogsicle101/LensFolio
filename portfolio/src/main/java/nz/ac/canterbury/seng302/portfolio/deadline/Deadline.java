package nz.ac.canterbury.seng302.portfolio.deadline;

import nz.ac.canterbury.seng302.portfolio.projects.Project;

import javax.naming.InvalidNameException;
import java.time.DateTimeException;
import java.time.LocalDate;

public class Deadline {
    private String name;
    private LocalDate deadlineDate;
    private Project project;

    public Deadline(String name, LocalDate deadlineDate, Project project) throws DateTimeException, InvalidNameException {
        if (project.getStartDate().isBefore(deadlineDate) && project.getEndDate().isAfter(deadlineDate)) {
            setName(name);
            this.deadlineDate = deadlineDate;
            this.project = project;
        } else {
            throw new DateTimeException("Deadline date not within project dates");
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidNameException{
        if (name.length() > 50) {
            throw new InvalidNameException("Name must be less than 50 characters");
        }
        this.name = name;
    }

    public LocalDate getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(LocalDate deadlineDate) {
        this.deadlineDate = deadlineDate;
    }
}
