package nz.ac.canterbury.seng302.portfolio.events;


import nz.ac.canterbury.seng302.portfolio.DateTimeFormat;
import nz.ac.canterbury.seng302.portfolio.projects.Project;


import javax.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.UUID;

@Entity
public class Event {

    private @Id
    @GeneratedValue
    UUID id;

    @ManyToOne()
    private Project project;

    @Column(length=50)
    private String name;
    private static int nameLengthRestriction = 50;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String startDateColour;
    private String endDateColour;
    private int typeOfEvent;


    public Event() {
    }

    public Event(Project project, String name, LocalDateTime startDate, LocalDateTime endDate, int typeOfEvent) {
        this.project = project;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.typeOfEvent = typeOfEvent;
    }
    public Event(Project project, String name, LocalDateTime startDate, LocalDateTime endDate) {
        this.project = project;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.typeOfEvent = 1;
    }

    public int getTypeOfEvent() {
        return typeOfEvent;
    }

    public void setTypeOfEvent(int typeOfEvent) {
        this.typeOfEvent = typeOfEvent;
    }

    public static int getNameLengthRestriction(){
        return nameLengthRestriction;
    }

    public String getStartDateFormatted() {

        return getStartDate().format(DateTimeFormat.timeDateMonthYear());
    }
    public String getEndDateFormatted() {

        return getEndDate().format(DateTimeFormat.timeDateMonthYear());
    }

    public LocalDate getStartAsLocal(){
        return this.startDate.toLocalDate();
    }

    public LocalDate getEndAsLocal(){
        return this.endDate.toLocalDate();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public LocalDateTime getStartDate() {
        return startDate;
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
}
