package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.events.Event;
import nz.ac.canterbury.seng302.portfolio.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
public class EventController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    @Autowired
    private final ProjectRepository projectRepository;

    @Autowired
    private final EventRepository eventRepository;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public EventController(ProjectRepository projectRepository, EventRepository eventRepository) {
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
    }

    //TODO add logging.


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
            logger.info("GET /getEventsList");
            List<Event> eventList = eventRepository.findAllByProjectId(projectId);
            HashMap<String, HashMap<String, String>> responseMap = new HashMap<>();

            for (Event event : eventList) {
                HashMap<String, String> eventDetails = new HashMap<>();
                eventDetails.put("id", event.getId().toString());
                eventDetails.put("name", event.getName());
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
    public ResponseEntity editEvent(
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

    @GetMapping("/checkEventChanges")
    public ResponseEntity<Object> checkEventChanges(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value="projectId") Long projectId) {
        try {
            logger.info("GET /checkEventChanges");

            //TODO should an event timeout, as in if a user starts editing an event, then closes the page, the event editing should timeout after a certain period to prevent events retaining their "being edited status".
            int id = PrincipalAttributes.getIdFromPrincipal(principal);
            UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                    .setId(id)
                    .build());
            List<UserRole> userRoles = userResponse.getRolesList();

            if(userRoles.contains(UserRole.TEACHER) || userRoles.contains(UserRole.COURSE_ADMINISTRATOR)) { // Checks that the user is allowed to access this.
                HashMap<String, String> results = new HashMap<>();


                List<Event> eventList = eventRepository.findAllByProjectId(projectId);
                for (Event event: eventList) {

                    if (event.isItBeingEdited()) {
                        results.put(event.getId().toString(),event.getUserNameThatIsEditing());

                    }
                }
                logger.info("/CheckEventChanges - Sent response");
                if (results.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(results,HttpStatus.OK);
                }

            } else {
                logger.warn("Post /userEditingEvent: User Unauthorized");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception err){
            logger.error("GET /checkEventChanges: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }


    }


    @PostMapping("/userEditingEvent")
    public ResponseEntity<Object> userEditingEvent(
            @RequestParam(value="eventId") UUID eventId,
            @AuthenticationPrincipal AuthState principal) {
        try{
            logger.info("POST /userEditingEvent");

            int id = PrincipalAttributes.getIdFromPrincipal(principal);
            UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                    .setId(id)
                    .build());
            List<UserRole> userRoles = userResponse.getRolesList();
            if(userRoles.contains(UserRole.TEACHER) || userRoles.contains(UserRole.COURSE_ADMINISTRATOR)) { // Checks that the user is allowed to access this.
                Event event = eventRepository.getById(eventId);
                event.setUserEditing(userResponse.getFirstName() + " " + userResponse.getLastName());
                event.setCurrentlyBeingEdited(true);
                eventRepository.save(event);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                logger.warn("Post /userEditingEvent: User Unauthorized");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }


        } catch (Exception err) {
            logger.error("Post /userEditingEvent: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }



    }

    @PostMapping("/userFinishedEditing")
    public ResponseEntity<Object> userFinishedEditing(
            @RequestParam(value="eventId") UUID eventId,
            @AuthenticationPrincipal AuthState principal
    ) {
        try{
            logger.info("POST /userFinishedEditing");

            int id = PrincipalAttributes.getIdFromPrincipal(principal);
            UserResponse userResponse = userAccountsClientService.getUserAccountById(GetUserByIdRequest.newBuilder()
                    .setId(id)
                    .build());
            List<UserRole> userRoles = userResponse.getRolesList();
            if(userRoles.contains(UserRole.TEACHER) || userRoles.contains(UserRole.COURSE_ADMINISTRATOR)) { // Checks that the user is allowed to access this.
                Event event = eventRepository.getById(eventId);
                event.setUserEditing(null);
                event.setCurrentlyBeingEdited(false);
                eventRepository.save(event);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                logger.warn("Post /userFinishedEditing: User Unauthorized");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }


        } catch (Exception err) {
            logger.error("Post /userFinishedEditing: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
