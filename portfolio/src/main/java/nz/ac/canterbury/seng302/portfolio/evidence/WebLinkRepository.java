package nz.ac.canterbury.seng302.portfolio.evidence;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository class for handling all the queries related to WebLink objects.
 */
public interface WebLinkRepository extends CrudRepository<WebLink, Integer> {

    /**
     * Finds an WebLink object by its id.
     */
    @Query
    WebLink findById(int id);

}
