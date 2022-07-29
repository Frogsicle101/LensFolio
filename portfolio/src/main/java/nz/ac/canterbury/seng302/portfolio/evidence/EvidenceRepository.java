package nz.ac.canterbury.seng302.portfolio.evidence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface EvidenceRepository extends CrudRepository<Evidence, Integer> {

    @Query
    Evidence findById(int id);
}
