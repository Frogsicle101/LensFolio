package nz.ac.canterbury.seng302.portfolio.projects.milestones;

import com.sun.istack.NotNull;
import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.naming.InvalidNameException;
import javax.persistence.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a Milestone entity.
 */
@Entity
public class Milestone {
    @Id
    private String id;

    @ManyToOne()
    private Project project;
    @Column(length = 50, nullable = false)
    private String name;
    @Column(nullable = false)
    @NotNull
    private LocalDate endDate;
    private String endDateColour;
    private int type;
    private static final int nameLengthRestriction = 50;


    /**
     * Default JPA milestone constructor.
     */
    protected Milestone() {
    }

    /**
     * Constructs an instance of the milestone object.s
     *
     * @param project The project in which the milestone occurs.
     * @param name    The name of the milestone.
     * @param endDate The end date of the milestone.
     * @param type    The type of the milestone.
     * @throws InvalidNameException If the milestone name is null or has length greater than fifty characters.
     */
    public Milestone(Project project, String name, LocalDate endDate, int type) throws InvalidNameException {


        if (name == null || name.length() > 50) { //useful for creating default milestones, but project.js includes validations for frontend milestone editing
            throw new InvalidNameException();
        }
        validateDate(project, endDate);
        this.id = UUID.randomUUID().toString();
        this.project = project;
        this.name = name;
        this.endDate = endDate;
        this.type = type;
    }


    /**
     * Checks that the end date occurs between the project's start and end dates.
     *
     * @param project The project defining the earliest and latest dates the end date can be.
     * @param endDate The end date being validated.
     * @throws DateTimeException If the end date is before the project start or after the project end.
     */
    public void validateDate(Project project, LocalDate endDate) throws DateTimeException {

        if (endDate.isAfter(project.getEndDate()) || endDate.isBefore(project.getStartDate())) {
            throw new DateTimeException("End date must occur during project");
        }
    }

    public static int getNameLengthRestriction() {
        return nameLengthRestriction;
    }

    /**
     * This sets the ID
     * <p>
     * SHOULD ONLY BE USED FOR TESTING PURPOSES
     *
     * @param id the UUID to be set
     */
    public void setUuid(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEndDate(LocalDate endDate) {
        this.validateDate(this.getProject(), endDate);
        this.endDate = endDate;
    }

    public void setEndDateColour(String colour) {
        this.endDateColour = colour;
    }

    public String getEndDateColour() {
        return endDateColour;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public String getEndDateFormatted() {
        return getEndDate().format(DateTimeFormat.dayDateMonthYear());
    }

    public int getType() {
        return type;
    }

    public Project getProject() {
        return this.project;
    }

    public void setType(int type) {
        this.type = type;
    }
}

