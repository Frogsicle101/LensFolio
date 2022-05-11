package nz.ac.canterbury.seng302.portfolio.projects.deadlines;

import nz.ac.canterbury.seng302.portfolio.projects.milestones.Milestone;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DeadlineRepository extends CrudRepository<Deadline, String> {
    @Query("select d from #{#entityName} as d where d.startDate IS NULL and d.project.id = ?1 order by d.dateTime")
    List<Deadline> findAllByProjectId(Long projectId);

    @Query
    Long countDeadlineByProjectId(Long projectId);

    @Query("select d from #{#entityName} as d where d.startDate IS NULL and d.project.id = ?1")
    List<Deadline> findAllByProjectIdOrderByEndDate(Long projectId);

    @Query
    Deadline getById(String eventId);
}
