package nz.ac.canterbury.seng302.portfolio.evidence;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository class for handling all the queries related to Category objects.
 */
public interface CategoryRepository extends CrudRepository<Category, Integer> {

}
