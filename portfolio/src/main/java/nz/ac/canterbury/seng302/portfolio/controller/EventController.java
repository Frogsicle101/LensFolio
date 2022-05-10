package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.RegexPatterns;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.projects.events.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;


@RestController
public class EventController {

    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;



    private final RegexPatterns regexPatterns = new RegexPatterns();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());



    public EventController(ProjectRepository projectRepository, EventRepository eventRepository) {
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
    }


    /**
     * Mapping for a put request to add event.
     * The method first parses the two date strings that are passed as request parameters.
     * They are being passed in, in a format called ISO_DATE_TIME, the parsers converts them from that to the standard
     * LocalDateTime format that we use.
     * <p>
     * The project is then grabbed from the repository by its ID.
     * If the project can't be found, it throws an EntityNotFoundException
     * <p>
     * The Event is then created with the parameters passed, and saved to the event repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param projectId id of project to add event to.
     * @param name      Name of event.
     * @param start     date of the start of the event
     * @param end       date of the end of the event.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addEvent")
    public ResponseEntity<Object> addEvent(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "eventName") String name,
            @RequestParam(value = "eventStart") String start,
            @RequestParam(value = "eventEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfEvent") int typeOfEvent
    ) {

        try {
            logger.info("PUT /addEvent");

            // eventStart and eventEnd return a string in the format "1986-01-28T11:38:00.01"
            // DateTimeFormatter.ISO_DATE_TIME helps parse that string by declaring its format.
            LocalDateTime eventStart = LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime eventEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);

            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            if (!regexPatterns.getTitleRegex().matcher(name).matches()) {

                String returnMessage = "Name does not match required pattern";
                logger.warn("PUT /addEvent: {}", returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }

            if (project.getStartDate().isAfter(LocalDate.from(eventStart)) || project.getEndDate().isBefore(LocalDate.from(eventEnd))) {
                String returnMessage = "Date(s) exist outside of project dates";
                logger.warn("PUT /addEvent: {}", returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }

            if (eventStart.isAfter(eventEnd)){
                String returnMessage = "Start date cannot be before end date";
                logger.warn("PUT /addEvent: {}", returnMessage);
                return new ResponseEntity<>("Start date cannot be before end date", HttpStatus.BAD_REQUEST);
            }

            Event event = new Event(project, name, eventStart, eventEnd.toLocalDate(), eventEnd.toLocalTime(), typeOfEvent);
            Event eventReturn = eventRepository.save(event);
            logger.info("PUT /addEvent: Success");
            return new ResponseEntity<>(eventReturn, HttpStatus.OK);

        } catch (EntityNotFoundException err) {
            logger.warn("PUT /addEvent: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException err) {
            logger.warn("PUT /addEvent: {}", err.getMessage());
            return new ResponseEntity<>("Could not parse date(s)", HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("PUT /addEvent: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Mapping for a delete request for event.
     * Trys to find the event with the Id given.
     * If it can't find the event an exception is thrown and then caught, with the error being returned.
     * If it can find the event, it tries to delete the event and if successful returns OK.
     * @param eventId Id of event to be deleted.
     * @return A status code indicating request was successful, or failed.
     */
    @DeleteMapping("/deleteEvent")
    public ResponseEntity<String> deleteEvent(
            @RequestParam(value = "eventId") UUID eventId
    ) {
        try{
            logger.info("DELETE: /deleteEvent");
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + eventId + " was not found"
            ));
            eventRepository.delete(event);
            logger.info("DELETE: /deleteEvent: Success");
            return new ResponseEntity<>(HttpStatus.OK);

        } catch(EntityNotFoundException err) {
            logger.warn("DELETE: /deleteEvent: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception err){
            logger.error("DELETE: /deleteEvent: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Mapping for a post request to edit an event.
     * The method first gets the event from the repository. If the event cannot be retrieved, it throws an EntityNotFound exception.
     * <p>
     * The method then parses the date strings that is passed as a request parameter.
     * The parsers convert the dates to the standard LocalDateTime format.
     * <p>
     * The Event is then edited with the parameters passed, and saved to the event repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param eventId     the ID of the event to be edited.
     * @param name        the new name of the event.
     * @param start       the new start date and time of the event.
     * @param end         the new end date and time of the event.
     * @param typeOfEvent the new type of the event.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/editEvent")
    public ResponseEntity<String> editEvent(
            @RequestParam(value = "eventId") UUID eventId,
            @RequestParam(value = "eventName") String name,
            @RequestParam(value = "eventStart") String start,
            @RequestParam(value = "eventEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfEvent") int typeOfEvent
    ) {
        try {
            logger.info("POST /editEvent");
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + eventId + " was not found"
            ));

            // eventStart and eventEnd return a string in the format "1986-01-28T11:38:00.01"
            // DateTimeFormatter.ISO_DATE_TIME helps parse that string by declaring its format.
            LocalDateTime eventStart = LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime eventEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);


            if (!regexPatterns.getTitleRegex().matcher(name).matches()) {
                String returnMessage = "Name does not match required pattern";
                logger.warn("POST /editEvent: {}", returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }

            Project project = event.getProject();
            if (project.getStartDate().isAfter(LocalDate.from(eventStart)) || project.getEndDate().isBefore(LocalDate.from(eventEnd))) {
                String returnMessage = "Date(s) exist outside of project dates";
                logger.warn("POST /editEvent: {}", returnMessage);
                return new ResponseEntity<>("Date(s) exist outside of project dates", HttpStatus.BAD_REQUEST);
            }

            event.setName(name);
            event.setStartDate(eventStart);
            event.setDateTime(eventEnd);
            event.setType(typeOfEvent);
            eventRepository.save(event);
            logger.info("POST /editEvent: Success");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.warn("POST /editEvent: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException err) {
            logger.warn("POST /editEvent: {}", err.getMessage());
            return new ResponseEntity<>("Could not parse date(s)", HttpStatus.BAD_REQUEST);
        } catch(Exception err) {
            logger.error("POST /editEvent: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    /**
     * Gets the list of events in a project and returns it.
     * @param projectId The projectId to get the events from this project
     * @return A ResponseEntity with the events or an error
     */
    @GetMapping("/getEventsList")
    public ResponseEntity<Object> getEventsList(
            @RequestParam(value="projectId") Long projectId
    ){
        try {
            logger.info("GET /getEventsList");
            List<Event> eventList = eventRepository.findAllByProjectIdOrderByStartDate(projectId);
            eventList.sort(Comparator.comparing(Event::getStartDate));
            logger.info("GET /getEventsList: Success");
            return new ResponseEntity<>(eventList, HttpStatus.OK);
        } catch(Exception err){
            logger.error("GET /getEventsList: {}", err.getMessage() );
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets the list of events by date and returns it.
     * @param date The date to get the events 
     * @return A ResponseEntity with the events or an error
     */
    @GetMapping("/getEventsListByDate")
    public ResponseEntity<Object> getEventsListByDate(
            @RequestParam(value="date") String date
    ){
        try {
            logger.info("GET /getEventsListByDate");
            List<Event> eventList = eventRepository.findAllByDate(date);
            eventList.sort(Comparator.comparing(Event::getStartDate));
            logger.info("GET /getEventsListByDate: Success");
            return new ResponseEntity<>(eventList, HttpStatus.OK);
        } catch(Exception err){
            logger.error("GET /getEventsListByDate: {}", err.getMessage() );
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Returns a single event from the id that was given
     * @param eventId the event id
     * @return a single event
     */
    @GetMapping("/getEvent")
    public ResponseEntity<Object> getEvent(
            @RequestParam(value="eventId") UUID eventId
    ){
        try {
            logger.info("GET /getEventsList");
            Event event = eventRepository.findById(eventId).orElseThrow();
            logger.info("GET /getEventsList: Success");
            return new ResponseEntity<>(event, HttpStatus.OK);
        } catch(NoSuchElementException err){
            logger.warn("GET /getEventsList: {}", err.getMessage());
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception err){
            logger.error("GET /getEventsList: {}", err.getMessage() );
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }









}
