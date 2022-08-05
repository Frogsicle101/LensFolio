package nz.ac.canterbury.seng302.portfolio.evidence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository class for handling all the queries related to Skill objects.
 */
public interface SkillRepository extends CrudRepository<Skill, Integer> {


    /**
     * Find a skill by its ID
     *
     * @param id -  the integer id of the skills
     * @return The skill object
     */
    @Query
    Skill findById(int id);

    /**
     * Finds a Skill object by its name.
     */
    @Query
    Skill findByNameIgnoreCase(String name);


    /**
     * Find all skills by a users ID
     *
     * @param userId -  the integer id of the user who has the skill
     * @return The list of skill objects
     */
    @Query
    List<Skill> findSkillsByEvidenceUserId(@Param("userId") int userId);
}
