package nz.ac.canterbury.seng302.portfolio.sprints;

import nz.ac.canterbury.seng302.portfolio.projects.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface SprintRepository extends CrudRepository<Sprint, UUID> {

    @Query
    List<Sprint> findAllByProjectId(Long projectId);

    @Query
    List<Sprint> findAllByIdNot(UUID id);

    @Query
    Sprint getSprintById(UUID id);

    @Query
    List<Sprint> getAllByProjectOrderByEndDateDesc(Project project);


    @Query
    List<Sprint> getAllByProjectOrderByStartDateAsc(Project project);




}
