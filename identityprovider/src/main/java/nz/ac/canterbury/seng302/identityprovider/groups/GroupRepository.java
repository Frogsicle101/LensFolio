package nz.ac.canterbury.seng302.identityprovider.groups;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface GroupRepository extends CrudRepository<Group, Integer> {

    @Query
    Optional<Group> findByShortName(String shortName);

    @Query
    Optional<Group> findByLongName(String longName);

    Group getGroupById(Integer groupId);
}
