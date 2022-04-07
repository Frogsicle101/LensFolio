package nz.ac.canterbury.seng302.portfolio.userPrefs;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserSortPrefRepository extends CrudRepository<UserPrefs, Integer> {

    @Query
    String findUserPrefsByUserId(long userId);
}
