package nz.ac.canterbury.seng302.portfolio.projects;

import com.google.type.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ProjectService {

    @Autowired
    private ProjectRepository repo;

    public void updateTimeDeactivated(Long id, DateTime timeDeactivated) {
        repo.deactivateProject(id, timeDeactivated);
    }
}
