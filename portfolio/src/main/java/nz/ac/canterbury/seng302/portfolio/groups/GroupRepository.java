package nz.ac.canterbury.seng302.portfolio.groups;

import nz.ac.canterbury.seng302.portfolio.projects.events.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<Group, Long> {

}
