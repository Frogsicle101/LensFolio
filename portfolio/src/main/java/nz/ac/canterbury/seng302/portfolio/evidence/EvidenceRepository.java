package nz.ac.canterbury.seng302.portfolio.evidence;

import org.springframework.data.repository.CrudRepository;

public interface EvidenceRepository extends CrudRepository<Evidence, Integer> {

    Evidence findById(int id);
}
