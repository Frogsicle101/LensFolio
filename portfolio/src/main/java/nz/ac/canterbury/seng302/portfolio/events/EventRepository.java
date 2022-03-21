package nz.ac.canterbury.seng302.portfolio.events;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends CrudRepository<Event, UUID> {
    @Query
    List<Event> findAllByProjectId(Long projectId);
}
