package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.DTO.EditEvent;
import nz.ac.canterbury.seng302.portfolio.events.Event;
import nz.ac.canterbury.seng302.portfolio.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class EventController {

    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

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
     *
     * The project is then grabbed from the repository by its ID.
     * If the project can't be found, it throws an EntityNotFoundException
     *
     * The Event is then created with the parameters passed, and saved to the event repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param projectId id of project to add event to.
     * @param name Name of event.
     * @param start date of the start of the event
     * @param end date of the end of the event.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addEvent")
    public ResponseEntity<String> addEvent(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "eventName") String name,
            @RequestParam(value = "eventStart")  String start,
            @RequestParam(value = "eventEnd") String end,
            @RequestParam(defaultValue = "1",value = "typeOfEvent") int typeOfEvent
    ) {
        try {
            // eventStart and eventEnd return a string in the format "1986-01-28T11:38:00.01"
            // DateTimeFormatter.ISO_DATE_TIME helps parse that string by declaring its format.
            LocalDateTime eventStart = LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime eventEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);

            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            Event event = new Event(project, name, eventStart, eventEnd, typeOfEvent);
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


    @GetMapping("/getEventsList")
    public ResponseEntity<Object> getEventsList(
            @RequestParam(value="projectId") Long projectId,
            @AuthenticationPrincipal AuthState principal
    ){
        try {
            //TODO return event list in order of dates
            logger.info("GET /getEventsList");
            List<Event> eventList = eventRepository.findAllByProjectId(projectId);
            HashMap<String, HashMap<String, String>> responseMap = new HashMap<>();

            for (Event event : eventList) {
                HashMap<String, String> eventDetails = new HashMap<>();
                eventDetails.put("id", event.getId().toString());
                eventDetails.put("name", event.getName());
                eventDetails.put("start", event.getStartDateFormatted());
                eventDetails.put("end", event.getEndDateFormatted());
                eventDetails.put("start", event.getStartDate().toString());
                eventDetails.put("end", event.getEndDate().toString());
                eventDetails.put("startFormatted", event.getEndDateFormatted());
                eventDetails.put("endFormatted", event.getEndDateFormatted());
                eventDetails.put("typeOfEvent", String.valueOf(event.getTypeOfEvent()));
                responseMap.put(event.getId().toString(), eventDetails);
            }
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        } catch(Exception err){
            logger.error("GET /getEventsList");
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
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + eventId + " was not found"
            ));
            eventRepository.delete(event);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch(EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception err){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/editEvent")
    public ResponseEntity<Object> editEvent(
            @RequestParam(value = "eventId") UUID eventId,
            @RequestParam(value = "eventName") String name,
            @RequestParam(value = "eventStart")  String start,
            @RequestParam(value = "eventEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfEvent") int typeOfEvent
    )
    {
        try{
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + eventId + " was not found"
            ));

            // eventStart and eventEnd return a string in the format "1986-01-28T11:38:00.01"
            // DateTimeFormatter.ISO_DATE_TIME helps parse that string by declaring its format.
            LocalDateTime eventStart = LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime eventEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);


            event.setName(name);
            event.setStartDate(eventStart);
            event.setEndDate(eventEnd);
            event.setTypeOfEvent(typeOfEvent);
            eventRepository.save(event);
            
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(EntityNotFoundException err) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch(Exception err){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @CrossOrigin
    @GetMapping(value = "/notifications", consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribeToNotifications(@AuthenticationPrincipal AuthState principal) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        try {
            logger.info("Subscribing user: " + userId);
            emitter.send(SseEmitter.event().name("INIT"));
        } catch (IOException e) {
            logger.warn("Not subscribing users");
        }
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }

    @PostMapping("/eventEdit")
    public void sendEventToClients(@AuthenticationPrincipal AuthState editor,
                                   @RequestParam UUID eventId) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                .setId(eventEditorID)
                .build());
        logger.info("Event id " + eventId + " is being edited by user: " + eventEditorID + " " + userResponse.getFirstName() + " " + userResponse.getLastName());
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(eventId);
            editEvent.setUserId(eventEditorID);
            editEvent.setUserName(userResponse.getFirstName() + " " + userResponse.getLastName());

            try {
                emitter.send(SseEmitter.event().name("editEvent")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }


    @PostMapping("/userFinishedEditing")
    public void userFinishedEditing(
            @RequestParam(value="eventId") UUID eventId,
            @AuthenticationPrincipal AuthState editor
    ) {
        int eventEditorID = PrincipalAttributes.getIdFromPrincipal(editor);
        logger.info("Event id " + eventId + " is no longer being edited by user: " + eventEditorID);
        for (SseEmitter emitter : emitters) {
            EditEvent editEvent = new EditEvent();
            editEvent.setEventId(eventId);
            editEvent.setUserId(eventEditorID);
            try {
                emitter.send(SseEmitter.event().name("editEventFinished")
                        .data(editEvent));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
