package nz.ac.canterbury.seng302.portfolio.projects.events;


import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.deadlines.Deadline;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Inheritance
public class Event extends Deadline {

    private LocalDateTime startDate;
    private String startDateColour;

    private int typeOfEvent;

    public Event() {
    }

    public Event(Project project, String name, LocalDateTime startDate, LocalDate endDate, LocalTime endTime, int typeOfEvent) {
        super(project, name, endDate, endTime);
        this.startDate = startDate;
        this.typeOfEvent = typeOfEvent;
    }


    public int getTypeOfEvent() {
        return typeOfEvent;
    }

    public void setTypeOfEvent(int typeOfEvent) {
        this.typeOfEvent = typeOfEvent;
    }

    public String getStartDateFormatted() {

        return getStartDate().format(DateTimeFormat.timeDateMonthYear());
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

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
