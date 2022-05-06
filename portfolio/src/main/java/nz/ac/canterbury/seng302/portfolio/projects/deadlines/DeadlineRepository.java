package nz.ac.canterbury.seng302.portfolio.projects.deadlines;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface DeadlineRepository extends CrudRepository<Deadline, UUID> {
    @Query("select d from #{#entityName} as d where d.startDate IS NULL and d.project.id = ?1 order by d.dateTime")
    List<Deadline> findAllByProjectId(Long projectId);
}
