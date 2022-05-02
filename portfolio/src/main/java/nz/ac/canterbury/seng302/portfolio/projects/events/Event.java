package nz.ac.canterbury.seng302.portfolio.projects.events;


import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;

import javax.naming.InvalidNameException;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents an Event entity.
 */
@Entity
@Inheritance
public class Event extends Deadline {

    private LocalDateTime startDate;
    private String startDateColour;

    /**
     * Default JPA event constructor.
     */
    public Event() {
    }

    /**
     * Constructs an instance of the event object.
     *
     * @param project The project in which the event occurs.
     * @param name The name of the event.
     * @param startDate The start date and time of the event
     * @param endDate The end date of the event.
     * @param endTime The end time of the event.
     * @param type The type of the event.
     * @throws DateTimeException If the event's date does not occur between the project's start and end dates.
     * @throws InvalidNameException If the event's name is null or has length greater than fifty characters.
     */
    public Event(Project project, String name, LocalDateTime startDate, LocalDate endDate, LocalTime endTime, int type) throws DateTimeException, InvalidNameException {
        super(project, name, endDate, endTime, type);
        this.startDate = startDate;
    }

    public void validateDate(Project project, LocalDate endDate, LocalDateTime startDate) throws DateTimeException {
        super.validateDate(project, endDate);
        if (startDate.toLocalDate().isAfter(project.getEndDate()) || startDate.toLocalDate().isBefore(project.getStartDate())) {
            throw new DateTimeException("Start date must occur within project");
        }
    }


    public String getStartDateFormatted() { return getStartDate().format(DateTimeFormat.timeDateMonthYear()); }

    public LocalDateTime getStartDate() { return startDate; }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public String getStartDateColour() {
        return startDateColour;
    }

    public void setStartDateColour(String startDateColour) {
        this.startDateColour = startDateColour;
    }
}
