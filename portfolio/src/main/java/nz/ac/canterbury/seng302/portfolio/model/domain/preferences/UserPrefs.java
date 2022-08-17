package nz.ac.canterbury.seng302.portfolio.model.domain.preferences;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserPrefs {

    @Id
    @Column(unique = true)
    private int userId;

    private String listSortPref;

    private boolean isAscending;

    /**
     * Constructs a UserPrefs object to be stored in the database.
     *
     * @param userId       The id of the user to be stored
     * @param listSortPref The sorting preference of the user. This should take the form of 'field-order',
     *                     e.g. 'name-decreasing' or 'aliases-ascending'
     */
    public UserPrefs(int userId, String listSortPref, boolean isAscending) {
        this.userId = userId;
        this.listSortPref = listSortPref;
        this.isAscending = isAscending;
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
                "User[id=%d, listSortPref='%s', isAscending='%b']",
                userId, listSortPref,isAscending);
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

    public boolean getIsAscending() { return isAscending;}

    public void setListSortPref(String listSortPref) {
        this.listSortPref = listSortPref;
    }
}
