package nz.ac.canterbury.seng302.portfolio.userPrefs;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interface that defines how to interact with the database. Spring boot does the hard work under the hood
 * to actually implement these functions.
 * @see  <a href="https://spring.io/guides/gs/accessing-data-jpa">https://spring.io/guides/gs/accessing-data-jpa/</a>
 */
public interface UserPrefRepository extends CrudRepository<UserPrefs, Integer> {

    /**
     * Gets a user's preference 'object' from the database using the id of said user
     * @return The user's preferences, in the form of a UserPrefs object
     */
    UserPrefs findByUserId(int userId);

    /**
     * Changes a user's sorting preferences to the preference given.
     * Performing this will flush the persistence context before executing the query,
     * and then clear the persistence context after the query completes. This is because
     * we're changing data, so we have to flush and clear the persistence context to keep
     * it up-to-date.
     * @param userId The id of the user to change
     * @param preference The sorting preference of the user. This should take the form of 'field-order',
     *                   e.g. 'name-decreasing' or 'aliases-ascending'
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserPrefs u set u.listSortPref = :preference where u.userId = :userId")
    void changeSortPref(@Param("userId") int userId, @Param("pref") String preference);
}
