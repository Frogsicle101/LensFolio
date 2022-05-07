package nz.ac.canterbury.seng302.portfolio.projects.events;

import nz.ac.canterbury.seng302.portfolio.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.projects.sprints.SprintRepository;

import java.time.LocalDate;
import java.util.List;


public class EventHelper {


    private EventHelper() {
    }

    public static List<Event> setEventColours(Long projectId, EventRepository eventRepository, SprintRepository sprintRepository) {

        List<Event> eventList = eventRepository.findAllByProjectIdOrderByStartDate(projectId);
        List<Sprint> sprintList = sprintRepository.findAllByProjectId(projectId);
        for(Event event: eventList) {
            for (Sprint sprint: sprintList) {
                LocalDate eStart = LocalDate.from(event.getStartDate());
                LocalDate eEnd = LocalDate.from(event.getEndDate());
                LocalDate sStart = sprint.getStartDate();
                LocalDate sEnd = sprint.getEndDate();
                if ((eStart.isAfter(sStart) || eStart.isEqual(sStart)) && (eStart.isBefore(sEnd) || eStart.isEqual(sEnd))){
                    //Event start date is between or equal to sprint start and end dates.
                    event.setStartDateColour(sprint.getColour());
                    if(!sprint.getEventList().contains(event)) {
                        sprint.addEvent(event);
                    }
                }
                if ((eEnd.isAfter(sStart) || eEnd.isEqual(sStart)) && (eEnd.isBefore(sEnd) || eEnd.isEqual(sEnd))){
                    //Event end date is between or equal to sprint start and end dates.
                    event.setEndDateColour(sprint.getColour());
                    if(!sprint.getEventList().contains(event)) {
                        sprint.addEvent(event);
                    }
                }
                //Event spans over the entire sprint
                if (eStart.isBefore(sStart) && eEnd.isAfter(sEnd) && !sprint.getEventList().contains(event)) {
                    sprint.addEvent(event);
                }
            }
        }
        return eventList;
    }
}
