package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.events.Event;
import nz.ac.canterbury.seng302.portfolio.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@RestController
public class EventController {

    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;

    public EventController(ProjectRepository projectRepository, EventRepository eventRepository) {
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
    }


    @PutMapping("/addEvent")
    public ResponseEntity<String> addEvent(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "eventName") String name,
            @RequestParam(value = "eventStart")  String start,
            @RequestParam(value = "eventEnd") String end
    ) {
        try {
            // eventStart and eventEnd return a string in the format "1986-01-28T11:38:00.01"
            // DateTimeFormatter.ISO_DATE_TIME helps parse that string by declaring its format.
            LocalDateTime eventStart = LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime eventEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);
            Project project = projectRepository.getProjectById(projectId);
            if (project == null) {
                throw new EntityNotFoundException();
            }

            Event event = new Event(project, name, eventStart, eventEnd);
            eventRepository.save(event);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch(EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(DateTimeParseException err) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception err) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @DeleteMapping("/deleteEvent")
    public ResponseEntity<String> deleteEvent(
            @RequestParam(value = "eventId") UUID eventId
    ) {
        try{
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + eventId + "was not found"
            ));
            eventRepository.delete(event);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch(EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception err){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
