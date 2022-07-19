package nz.ac.canterbury.seng302.portfolio.projects.events;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;
import java.time.LocalDate;
import java.util.List;


public class EventHelper {

    /**
     *Helper Function for events to organize and return a list of events.
     * @param projectId
     * @param eventRepository
     * @param sprintRepository
     * @return
     */
    public static List<Event> setEventColours(Long projectId, EventRepository eventRepository, SprintRepository sprintRepository) {
        List<Event> eventList = eventRepository.findAllByProjectIdOrderByStartDate(projectId);
        List<Sprint> sprintList = sprintRepository.findAllByProjectId(projectId);
        for (Event event : eventList) {
            for (Sprint sprint : sprintList) {
                LocalDate eventStart = LocalDate.from(event.getStartDate());
                LocalDate eventEnd = LocalDate.from(event.getEndDate());
                LocalDate sprintStart = sprint.getStartDate();
                LocalDate sprintEnd = sprint.getEndDate();
                if ((eventStart.isAfter(sprintStart) || eventStart.isEqual(sprintStart)) && (eventStart.isBefore(sprintEnd) || eventStart.isEqual(sprintEnd))) {
                    //Event start date is between or equal to sprint start and end dates.
                    event.setStartDateColour(sprint.getColour());
                    if (!sprint.getEventList().contains(event)) {
                        sprint.addEvent(event);
                    }
                }
                if ((eventEnd.isAfter(sprintStart) || eventEnd.isEqual(sprintStart)) && (eventEnd.isBefore(sprintEnd) || eventEnd.isEqual(sprintEnd))) {
                    //Event end date is between or equal to sprint start and end dates.
                    event.setEndDateColour(sprint.getColour());
                    if (!sprint.getEventList().contains(event)) {
                        sprint.addEvent(event);
                    }
                }
                //Event spans over the entire sprint
                if (eventStart.isBefore(sprintStart) && eventEnd.isAfter(sprintEnd) && !sprint.getEventList().contains(event)) {
                    sprint.addEvent(event);
                }
            }
        }
        return eventList;
    }
}
