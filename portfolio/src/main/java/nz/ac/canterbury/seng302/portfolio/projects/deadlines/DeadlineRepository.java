package nz.ac.canterbury.seng302.portfolio.projects.deadlines;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface DeadlineRepository extends CrudRepository<Deadline, UUID> {
}
