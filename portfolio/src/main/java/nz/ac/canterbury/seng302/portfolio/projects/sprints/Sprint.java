package nz.ac.canterbury.seng302.portfolio.projects.sprints;
import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Entity // This maps Sprint to a table in the Db called "Sprint"
public class Sprint {
    @Id
    private String id; // @Id lets JPA know it's the objects ID

    @ManyToOne
    private Project project;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String colour;
    @OneToMany
    private List<Event> eventList;
    @OneToMany
    private List<Deadline> deadlineList;
    @OneToMany
    private List<Milestone> milestoneList;

    protected Sprint() {
    }

    /**
     * Constructor for Sprint
     *
     * @param project     Project the sprint belongs to.
     * @param name        Name of sprint.
     * @param startDate   Start date of sprint.
     * @param endDate     End date of sprint.
     * @param description description of sprint.
     * @param colour      colour of sprint.
     */
    public Sprint(Project project, String name, LocalDate startDate, LocalDate endDate, String description, String colour) {
        this.id = String.valueOf(UUID.randomUUID());
        this.project = project;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.colour = colour;
    }

    /**
     * Default Constructor for Sprint
     *
     * @param project Project the sprint belongs too.
     * @param name    Name of the Sprint.
     */
    public Sprint(Project project, String name, LocalDate startDate) {

        this.id = String.valueOf(UUID.randomUUID());
        this.project = project;
        this.name = name;
        this.startDate = startDate;
        this.endDate = (startDate).plusWeeks(3);
        this.description = "No description";
        this.colour = "#f554f5";
    }


    public Sprint(Project project, String name, LocalDate startDate, LocalDate endDate) {

        this.id = String.valueOf(UUID.randomUUID());
        this.project = project;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = "No description";
        this.colour = "#f554f5";
    }

    public void addEvent(Event event) {
        eventList.add(event);
    }

    public void addDeadline(Deadline deadline) { deadlineList.add(deadline); }

    public void addMilestone(Milestone milestone) { milestoneList.add(milestone); }

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public List<Deadline> getDeadlineList() { return deadlineList; }

    public void setDeadlineList(List<Deadline> deadlineList) { this.deadlineList = deadlineList; }

    public List<Milestone> getMilestoneList() { return milestoneList; }

    public void setMilestoneList(List<Milestone> milestoneList) { this.milestoneList = milestoneList; }


    public LocalDate getStartDate() {
        return startDate;
    }

    public String getStartDateFormatted() {
        return startDate.format(DateTimeFormat.dayDateMonthYear());
    }

    public String getEndDateFormatted() {
        return endDate.format(DateTimeFormat.dayDateMonthYear());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }


}

