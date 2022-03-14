package nz.ac.canterbury.seng302.portfolio.sprints;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;


@Entity // This maps Sprint to a table in the Db called "Sprint"
public class Sprint {
    private @Id UUID id; // @Id lets JPA know it's the objects ID
    private long projectId;
    private String name;
    private String startDate;
    private String endDate;
    private String description;
    private String colour;

    protected Sprint() {}

    /**
     * Constructor for Sprint
     * @param projectId Project the sprint belongs to.
     * @param name Name of sprint.
     * @param startDate Start date of sprint.
     * @param endDate End date of sprint.
     * @param description description of sprint.
     * @param colour colour of sprint.
     */
    public Sprint(long projectId, String name, String startDate, String endDate, String description, String colour) {
        this.id = UUID.randomUUID();
        this.projectId = projectId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.colour = colour;
    }

    /**
     * Default Constructor for Sprint
     * @param projectId Project the sprint belongs too.
     * @param name Name of the Sprint.
     */
    public Sprint(long projectId, String name, String startDate) {

        this.id = UUID.randomUUID();
        this.projectId = projectId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = LocalDate.parse(startDate).plusWeeks(3).toString();
        this.description = "No description";
        this.colour = "#f554f5";


    }

    /**
     * Getter for Id
     * @return UUID id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Setter for Id
     * @param id UUID id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Getter for project id
     * @return long projectId
     */
    public long getProjectId() {
        return projectId;
    }

    /**
     * Setter for project id
     * @param projectId ProjectId
     */
    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    /**
     * Getter for Name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for Name.
     * @param name Name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for Start Date
     * @return Start Date
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Setter for Start Date.
     * @param start_date Start Date.
     */
    public void setStartDate(String start_date) {
        this.startDate = start_date;
    }

    /**
     * Getter for End Date
     * @return End Date
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Setter for End Date.
     * @param end_date End Date.
     */
    public void setEndDate(String end_date) {
        this.endDate = end_date;
    }

    /**
     * Getter for Description.
     * @return Description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for Description.
     * @param description Description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for Colour
     * @return Colour
     */
    public String getColour() {
        return colour;
    }

    /**
     * Setter for Colour
     * @param colour Colour
     */
    public void setColour(String colour) {
        this.colour = colour;
    }
}
