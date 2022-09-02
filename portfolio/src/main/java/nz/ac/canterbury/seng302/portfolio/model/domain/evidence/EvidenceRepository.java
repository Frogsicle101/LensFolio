package nz.ac.canterbury.seng302.portfolio.model.domain.evidence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Repository class for handling all the queries related to Evidence objects.
 */
@Repository
public interface EvidenceRepository extends CrudRepository<Evidence, Integer> {

    /** Finds an Evidence object by its id. */
    @Query
    Optional<Evidence> findById(int id);

    /** Returns an arrayList of all the evidence for a user in order by date descending */
    @Query
    ArrayList<Evidence> findAllByUserIdOrderByOccurrenceDateDesc(int id);

    /** Returns an arrayList that contains all the evidence of a user of a certain category. */
    @Query
    ArrayList<Evidence> findAllByUserIdAndCategoriesContaining(int id, Category category);

    @Query
    List<Evidence> findAllByUserIdAndSkillsIsEmptyOrderByOccurrenceDateDesc(int userId);
}
