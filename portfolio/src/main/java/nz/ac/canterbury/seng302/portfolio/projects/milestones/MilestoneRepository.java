package nz.ac.canterbury.seng302.portfolio.projects.milestones;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MilestoneRepository extends CrudRepository<Milestone, UUID> {
}
