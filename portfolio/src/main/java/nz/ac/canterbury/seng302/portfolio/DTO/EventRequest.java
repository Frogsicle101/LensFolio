package nz.ac.canterbury.seng302.portfolio.DTO;


public class EventRequest {
    private Long projectId;
    private String eventName;
    private String eventStartDate;
    private String eventEndDate;


    public EventRequest() {
        super();
    }

    public EventRequest(Long projectId, String eventName, String eventStartDate, String eventEndDate) {
        super();
        this.projectId = projectId;
        this.eventName = eventName;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(String eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public String getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(String eventEndDate) {
        this.eventEndDate = eventEndDate;
    }
}
