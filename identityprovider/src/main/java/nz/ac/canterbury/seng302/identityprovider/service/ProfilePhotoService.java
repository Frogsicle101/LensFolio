package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.User;
import nz.ac.canterbury.seng302.identityprovider.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;

public class ProfilePhotoService {

    @Autowired
    UserRepository repository;

    public void updateProfileImage(int id, URL path) {
        User user = repository.findById(id);
        user.setProfileImagePath(path);
    }

    /**
     * FOR TESTING
     *
     * @param repository
     */
    public void setRepository(UserRepository repository) {
        this.repository = repository;
    }

}
