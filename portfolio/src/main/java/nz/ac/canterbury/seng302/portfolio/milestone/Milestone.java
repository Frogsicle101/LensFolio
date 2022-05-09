package nz.ac.canterbury.seng302.portfolio.milestone;

import nz.ac.canterbury.seng302.portfolio.projects.Project;

import javax.naming.InvalidNameException;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an instance of a milestone
 *
 * @author Daniel Pallesen
 */

@Entity
public class Milestone {

    private @Id
    @GeneratedValue
    UUID id;

    @ManyToOne()
    private Project project;

    @Column(length=50)
    private String name;
    private static final int nameLengthRestriction = 50;
    private LocalDateTime milestoneDate;
    private String milestoneColour;


    public Milestone() {}

    public Milestone(Project project, String name, LocalDateTime milestoneDate) throws InvalidNameException {
        this.project = project;
        setName(name);
        setMilestoneDate(milestoneDate);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidNameException {
        if (name.length() > nameLengthRestriction) {
            throw new InvalidNameException("Name must be less than " + String.valueOf(nameLengthRestriction) + " characters");
        }
        this.name = name;
    }

    public LocalDateTime getMilestoneDate() {
        return milestoneDate;
    }

    public void setMilestoneDate(LocalDateTime milestoneDate) {
        this.milestoneDate = milestoneDate;
    }

    public String getMilestoneColour() {
        return milestoneColour;
    }

    public void setMilestoneColour(String milestoneColour) {
        this.milestoneColour = milestoneColour;
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }
}
