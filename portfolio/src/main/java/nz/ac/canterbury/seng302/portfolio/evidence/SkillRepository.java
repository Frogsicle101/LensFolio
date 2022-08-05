package nz.ac.canterbury.seng302.portfolio.evidence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository class for handling all the queries related to Skill objects.
 */
public interface SkillRepository extends CrudRepository<Skill, Integer> {

    /**
     * Finds a Skill object by its id.
     */
    @Query
    Skill findById(int id);

    /**
     * Finds a Skill object by its name.
     */
    @Query
    Skill findByName(String name);
}
