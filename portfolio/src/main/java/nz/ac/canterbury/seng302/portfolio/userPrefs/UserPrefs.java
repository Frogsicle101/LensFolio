package nz.ac.canterbury.seng302.portfolio.userPrefs;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class UserPrefs {

    @Id
    @Column(unique = true)
    private int userId;

    private String preference;


    public UserPrefs(int userId, String preference) {
        this.userId = userId;
        this.preference = preference;
    }


    protected UserPrefs() {}


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }
}
