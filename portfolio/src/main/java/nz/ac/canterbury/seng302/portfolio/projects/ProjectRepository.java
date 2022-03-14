package nz.ac.canterbury.seng302.portfolio.projects;

import com.google.type.DateTime;
import com.sun.istack.NotNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

    @Modifying
    @Query("update Project p set p.time_deactivated = :time_deactivated where p.id = :id")
    void deactivateProject(@Param(value = "id") long id, @Param(value= "time_deactivated") DateTime time_deactivated);

    @Query
    Project getProjectById(Long projectId);


}
