package nz.ac.canterbury.seng302.portfolio.projects;

import com.google.type.DateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

    @Modifying
    @Query("update Project p set p.timeDeactivated = :timeDeactivated where p.id = :id")
    void deactivateProject(@Param(value = "id") long id, @Param(value= "timeDeactivated") DateTime timeDeactivated);

    @Query
    Project getProjectById(Long projectId);



    @Query
    Project getProjectByName(String projectName);

}
