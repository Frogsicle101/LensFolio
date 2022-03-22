package nz.ac.canterbury.seng302.portfolio.events;

import com.google.type.DateTime;
import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.UUID;

@Entity
public class Event {

    private @Id
    @GeneratedValue
    UUID id;

    private long projectId;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String startDateColour;
    private String endDateColour;


    public Event() {
    }

    public Event(long projectId, String name, LocalDateTime startDate, LocalDateTime endDate) {
        this.projectId = projectId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getStartDateColour() {
        return startDateColour;
    }

    public void setStartDateColour(String startDateColour) {
        this.startDateColour = startDateColour;
    }

    public String getEndDateColour() {
        return endDateColour;
    }

    public void setEndDateColour(String endDateColour) {
        this.endDateColour = endDateColour;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public String getStartDateFormatted() {

        return getStartDate().format(DateTimeFormat.timeDateMonthYear());
    }
    public String getEndDateFormatted() {

        return getEndDate().format(DateTimeFormat.timeDateMonthYear());
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
