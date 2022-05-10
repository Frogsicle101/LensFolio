package nz.ac.canterbury.seng302.portfolio.projects.events;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends CrudRepository<Event, UUID> {
    @Query
    List<Event> findAllByProjectIdOrderByStartDate(Long projectId);

    @Query
    Event getById(UUID eventId);

    @Query("select e from #{#entityName} as e where e.startDate <= ?1 and e.endDate >= ?1")
    List<Event> findAllByDate(String date);
}
