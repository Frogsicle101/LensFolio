package nz.ac.canterbury.seng302.portfolio.projects.milestones;

import com.sun.istack.NotNull;
import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.Project;

import javax.naming.InvalidNameException;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a Milestone entity.
 */
@Entity
@Inheritance
public abstract class Milestone {

    private @Id
    @GeneratedValue
    UUID id;

    @ManyToOne()
    private Project project;
    @Column(length=50, nullable = false)
    private String name;
    @Column(nullable = false)
    @NotNull
    private LocalDate endDate;
    private String endDateColour;
    private static final int nameLengthRestriction = 50;


    /**
     * Default JPA milestone constructor.
     */
    protected Milestone() {
    }

    /**
     * Constructs an instance of the milestone object.
     *
     * @param project The project in which the milestone occurs.
     * @param name The name of the milestone.
     * @param endDate The end date of the milesone.
     * @throws InvalidNameException If the milestone name is null or has length greater than fifty characters.
     */
    public Milestone(Project project, String name, LocalDate endDate) throws InvalidNameException {
        if (name == null || name.length() > 50) {
            throw new InvalidNameException();
        }
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
