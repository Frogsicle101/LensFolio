package nz.ac.canterbury.seng302.portfolio.groups;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<Group, Long> {

    @Query
    Group getGroupByGroupId(Long groupId);
}
