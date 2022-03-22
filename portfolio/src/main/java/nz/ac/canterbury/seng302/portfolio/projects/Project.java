package nz.ac.canterbury.seng302.portfolio.projects;

import com.google.type.DateTime;
import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;


@Entity // Maps Project object to a table in the database called "Project"
public class Project {

    private @Id @GeneratedValue long id; // @Id lets JPA know it's the objects ID
    private long plannerId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private DateTime timeDeactivated;

    protected Project() {}

    /**
     * Constructor for Project.
     *
     * @param plannerId  The planner on which the project was created.
     * @param name        Name of project.
     * @param startDate  Start date of project.
     * @param endDate    End date of project.
     * @param description description of project.
     */
    public Project(long plannerId, String name, LocalDate startDate, LocalDate endDate, String description) {
        this.plannerId = plannerId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.timeDeactivated = null;
    }

    /**
     * Default Constructor for a Project.
     *
     * @param name Name of the Project.
     */
    public Project(long plannerId, String name) {
        LocalDate localDate = LocalDate.now();
        this.plannerId = plannerId;
        this.name = name;
        this.startDate = localDate;
        this.endDate = localDate.plusMonths(8);
        this.description = "No description";
        this.timeDeactivated = null;
    }

    /**
     * Getter for ID.
     *
     * @return UUID id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter for ID.
     *
     * @param id UUID id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for Planner ID.
     *
     * @return long planner_id.
     */
    public long getPlannerId() {
        return plannerId;
    }

    /**
     * Setter for Planner ID.
     *
     * @param plannerId planner ID.
     */
    public void setPlannerId(long plannerId) {
        this.plannerId = plannerId;
    }

    /**
     * Getter for Name.
     *
     * @return name.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for Name.
     *
     * @param name Name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for Start Date.
     *
     * @return Project start date.
     */
    public String getStartDate() {
        return startDate.toString();
    }

    public String getStartDateFormatted(){
        return startDate.format(DateTimeFormat.dayDateMonthYear());
    }

    /**
     * Setter for Start Date.
     *
     * @param startDate Project start date.
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter for End Date
     *
     * @return End Date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    public String getEndDateFormatted(){
        return endDate.format(DateTimeFormat.dayDateMonthYear());
    }

    /**
     * Setter for End Date.
     *
     * @param endDate End Date.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter for Description.
     *
     * @return Description of project.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for Description.
     *
     * @param description Description of project.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for time deactivated.
     *
     * @return DateTime planner deactivation date and time.
     */
    public DateTime getTimeDeactivated() {
        return timeDeactivated;
    }

    /**
     * Setter for time deactivated.
     *
     * @param timeDeactivated the time of project deactivation.
     */
    public void setTimeDeactivated(DateTime timeDeactivated) {
        this.timeDeactivated = timeDeactivated;
    }


}




