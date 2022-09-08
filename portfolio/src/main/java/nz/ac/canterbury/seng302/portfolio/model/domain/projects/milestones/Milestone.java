package nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.service.DateTimeService;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.service.OccasionService;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
    private LocalDate endDate;
    private int type;
    private static final int NAME_LENGTH_RESTRICTION = 50;


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
     */
    public Milestone(Project project, String name, LocalDate endDate, int type) throws CheckException, DateTimeException {
        OccasionService.validateDate(project, endDate);
        this.id = UUID.randomUUID().toString();
        this.project = project;
        this.name = name;
        this.endDate = endDate;
        if (type < 1) {
            throw new CheckException("Invalid occasion type");
        }
        this.type = type;
    }

    public static int getNameLengthRestriction() {
        return NAME_LENGTH_RESTRICTION;
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
        OccasionService.validateDate(this.getProject(), endDate);
        this.endDate = endDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public String getEndDateFormatted() {
        return getEndDate().format(DateTimeService.dayDateMonthYear());
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

