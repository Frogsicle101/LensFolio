package nz.ac.canterbury.seng302.portfolio.projects.milestones;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MilestoneRepository extends CrudRepository<Milestone, UUID> {
    @Query("select m from #{#entityName} as m where m.startDate IS NULL and m.dateTime IS NULL and m.project.id = ?1 order by m.endDate")
    List<Milestone> findAllByProjectIdOrderByEndDate(Long projectId);
}
