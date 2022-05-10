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

    @Query("select name from #{#entityName} as m where m.startDate <= ? and m.endDate >= ?")
    List<Event> findAllByDate(String date);
}
