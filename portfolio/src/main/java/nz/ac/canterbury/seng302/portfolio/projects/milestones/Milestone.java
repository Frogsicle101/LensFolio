package nz.ac.canterbury.seng302.portfolio.projects.milestones;

import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.Project;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;

@Entity
@Inheritance
public abstract class Milestone {

    private @Id
    @GeneratedValue
    UUID id;

    @ManyToOne()
    private Project project;

    @Column(length=50)
    private String name;
    private static final int nameLengthRestriction = 50;
    private LocalDate endDate;
    private String endDateColour;


    protected Milestone() {
    }

    public Milestone(Project project, String name, LocalDate endDate) {
        this.project = project;
        this.name = name;
        this.endDate = endDate;
    }

    public static int getNameLengthRestriction() {
        return nameLengthRestriction;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public void setEndDateColour(String colour) { this.endDateColour = colour; }

    public String getEndDateColour() {
        return endDateColour;
    }

    public LocalDate getEndDate() { return this.endDate; }

    public String getEndDateFormatted() {
        return getEndDate().format(DateTimeFormat.dayDateMonthYear());
    }

}
