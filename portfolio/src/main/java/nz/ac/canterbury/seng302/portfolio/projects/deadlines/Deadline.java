package nz.ac.canterbury.seng302.portfolio.projects.deadlines;

import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;

import javax.naming.InvalidNameException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a Deadline entity.
 */
@Entity
public class Deadline extends Milestone {

//    @Column(nullable = false) //FIXME this should be uncommented but it breaks something:(
    private LocalTime endTime;
    private LocalDateTime dateTime;


    /**
     * Default JPA deadline constructor.
     */
    public Deadline() {
    }


    /**
     * Constructs an instance of the deadline object.
     *
     * @param project The project in which the deadline occurs.
     * @param name The name of the deadline.
     * @param endDate The end date of the deadline.
     * @param endTime The end time of the deadline.
     * @param type The type of the deadline.
     * @throws DateTimeException If the deadline's date does not occur between the project's start and end dates.
     * @throws InvalidNameException If the deadline's name is null or has length greater than fifty characters.
     */
    public Deadline(Project project, String name, LocalDate endDate, LocalTime endTime, int type) throws DateTimeException, InvalidNameException {
        super(project, name, endDate, type);
        validateDate(project, endDate);
        this.endTime = endTime;
        this.dateTime = LocalDateTime.of(endDate, endTime);
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


    /**
     * Formats the object's date to the form "hh:mma E d MMMM y"
     * @return The formatted date.
     */
    @Override
    public String getEndDateFormatted() {
        return LocalDateTime.of(this.getEndDate(), this.endTime).format(DateTimeFormat.timeDateMonthYear());
    }


    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(LocalDateTime eventEnd) {
        this.dateTime = eventEnd;
        setEndTime(eventEnd.toLocalTime());
        setEndDate(eventEnd.toLocalDate());
    }
}
