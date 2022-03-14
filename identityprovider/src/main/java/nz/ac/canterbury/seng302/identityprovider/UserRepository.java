package nz.ac.canterbury.seng302.identityprovider;

import org.springframework.data.repository.CrudRepository;

/**
 * Interface that defines how to interact with the database. Spring boot does the hard work under the hood
 * to actually implement these functions.
 * @see  <a href="https://spring.io/guides/gs/accessing-data-jpa">https://spring.io/guides/gs/accessing-data-jpa/</a>
 */
public interface UserRepository extends CrudRepository <User, Integer> {
    /**
     * Gets a user object from the database using the id
     * @param id The user id
     * @return A user object, or null if none exist with that id.
     */
    User findById(int id);

    /**
     * Gets a user object from the database using the username
     * @param username The username
     * @return A user object, or null if none exist with that username
     */
    User findByUsername(String username);
}
