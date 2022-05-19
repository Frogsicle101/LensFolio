package nz.ac.canterbury.seng302.portfolio.groups;

import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class GroupService {

    private final GroupRepository repository;

    public GroupService(GroupRepository repository) {
        this.repository = repository;
    }

    public void addUserToGroup(long groupId, int userId) {
        Optional<Group> optionalGroup = repository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        Group group = optionalGroup.get();
        group.addUserToGroup(userId);
        repository.save(group);

    }

    public void removeUserFromGroup(long groupId, int userId) {
        Optional<Group> optionalGroup = repository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }

        Group group = optionalGroup.get();
        group.removeUserFromGroup(userId);
        repository.save(group);

    }


}
