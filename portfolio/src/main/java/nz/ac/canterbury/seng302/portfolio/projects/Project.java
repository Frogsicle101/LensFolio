package nz.ac.canterbury.seng302.portfolio.projects;

import com.google.type.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;


@Entity // Maps Project object to a table in the database called "Project"
public class Project {

    private @Id @GeneratedValue long id; // @Id lets JPA know it's the objects ID
    private long planner_id;
    private String name;
    private String start_date;
    private String end_date;
    private String description;
    private DateTime time_deactivated;

    protected Project() {}

    /**
     * Constructor for Project.
     *
     * @param planner_id  The planner on which the project was created.
     * @param name        Name of project.
     * @param start_date  Start date of project.
     * @param end_date    End date of project.
     * @param description description of project.
     */
    public Project(long planner_id, String name, String start_date, String end_date, String description) {
        this.planner_id = planner_id;
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.description = description;
        this.time_deactivated = null;
    }

    /**
     * Default Constructor for a Project.
     *
     * @param name Name of the Project.
     */
    public Project(long planner_id, String name) {
        LocalDate localDate = LocalDate.now();
        this.planner_id = planner_id;
        this.name = name;
        this.start_date = localDate.toString();
        this.end_date = localDate.plusMonths(8).toString();
        this.description = "No description";
        this.time_deactivated = null;
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
        return planner_id;
    }

    /**
     * Setter for Planner ID.
     *
     * @param planner_id planner ID.
     */
    public void setPlannerId(long planner_id) {
        this.planner_id = planner_id;
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
        return start_date;
    }

    /**
     * Setter for Start Date.
     *
     * @param start_date Project start date.
     */
    public void setStartDate(String start_date) {
        this.start_date = start_date;
    }

    /**
     * Getter for End Date
     *
     * @return End Date
     */
    public String getEndDate() {
        return end_date;
    }

    /**
     * Setter for End Date.
     *
     * @param end_date End Date.
     */
    public void setEndDate(String end_date) {
        this.end_date = end_date;
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
        return time_deactivated;
    }

    /**
     * Setter for time deactivated.
     *
     * @param time_deactivated the time of project deactivation.
     */
    public void setTimeDeactivated(DateTime time_deactivated) {
        this.time_deactivated = time_deactivated;
    }


}




