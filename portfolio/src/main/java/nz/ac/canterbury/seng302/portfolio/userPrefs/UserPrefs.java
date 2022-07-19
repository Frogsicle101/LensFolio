package nz.ac.canterbury.seng302.portfolio.userPrefs;


import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class UserPrefs {

    @Id
    @Column(unique = true)
    private int userId;

    private String listSortPref;

    /**
     * Constructs a UserPrefs object to be stored in the database.
     *
     * @param userId       The id of the user to be stored
     * @param listSortPref The sorting preference of the user. This should take the form of 'field-order',
     *                     e.g. 'name-decreasing' or 'aliases-ascending'
     */
    public UserPrefs(int userId, String listSortPref) {
        this.userId = userId;
        this.listSortPref = listSortPref;
    }

    /**
     * This constructor exists only for the sake of JPA.
     * Don't use this constructor directly.
     */
    protected UserPrefs() {
    }

    @Override
    public String toString() {
        return String.format(
                "User[id=%d, listSortPref='%s']",
                userId, listSortPref);
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getListSortPref() {
        return listSortPref;
    }

    public void setListSortPref(String listSortPref) {
        this.listSortPref = listSortPref;
    }
}
