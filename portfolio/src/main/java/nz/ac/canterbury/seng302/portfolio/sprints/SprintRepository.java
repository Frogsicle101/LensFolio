package nz.ac.canterbury.seng302.portfolio.sprints;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SprintRepository extends CrudRepository<Sprint, UUID> {

    @Query
    List<Sprint> findAllByProjectId(Long projectId);

    @Query
    List<Sprint> findAllByIdNot(UUID id);




}
