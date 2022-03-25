package nz.ac.canterbury.seng302.portfolio.events;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import nz.ac.canterbury.seng302.portfolio.projects.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@DataJpaTest
class EventTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProjectRepository projectRepository;


    @Test
    void testWhenFindById_ThenReturnEvent(){
        // Given
        Project project = projectRepository.getProjectByName("Project Default");
        Event event = new Event(project, "TestEvent", LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        entityManager.persist(event);
        entityManager.flush();


        // When
        Event foundEvent = eventRepository.getById(event.getId());

        // Then
        assertThat(event.getId()).isEqualTo(foundEvent.getId());


    }

    @Test
    void testWhenDeleteById_ThenReturnListOfEventsWithoutDeletedEvent(){
        // Given
        Project project = projectRepository.getProjectByName("Project Default");
        Event event = new Event(project, "TestEvent", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        Event event2 = new Event(project, "TestEvent2", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        Event event3 = new Event(project, "TestEvent3", LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        entityManager.persist(event);
        entityManager.persist(event2);
        entityManager.persist(event3);
        entityManager.flush();


        // When
        eventRepository.deleteById(event.getId());
        List<Event> eventList = (List<Event>) eventRepository.findAll();

        // Then
        assertThat(event).isNotIn(eventList);
        assertThat(event2).isIn(eventList);
        assertThat(event3).isIn(eventList);


    }











}