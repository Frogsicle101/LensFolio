package nz.ac.canterbury.seng302.identityprovider;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.identityprovider.service.LoginService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * The object used to store Users in the database
 * <br>
 * These users have an automatically generated id which is the primary key for users in the database.
 * The attributes contained in this class reflect the attributes that would be passed/used in the user_accounts.proto
 * contract.
 *
 * @author Frederik Markwell
 */
@Entity
public class User {

    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true)
    private String username;

    private String pwhash;
    private String firstName;
    private String middleName;
    private String lastName;
    private String nickname;
    private String bio;
    private String pronouns;
    private String email;
    private String salt;
    @Column(length = 100000)
    private Timestamp accountCreatedTime;
    private final ArrayList<UserRole> roles = new ArrayList<>();

    private String imagePath;



    /**
     * Generic constructor used by JPA
     */
    protected User () {}


    /**
     * Constructs a new user object. Calculates and stores a hash of the given password with unique salt.
     * @param username - the username of the user
     * @param password - the password of the user
     * @param firstName - the first name of the user
     * @param lastName - the last name of the user
     * @param nickname - the nickname of the user
     * @param bio - the bio of the user
     * @param pronouns - the users personal pronouns
     * @param email - the email of the user
     * @param accountCreatedTime - the time the account was created
     */
    public User(String username, String password, String firstName, String middleName, String lastName, String nickname, String bio, String pronouns, String email, Timestamp accountCreatedTime) {
        this.username = username;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.bio = bio;
        this.pronouns = pronouns;
        this.email = email;
        this.roles.add(UserRole.STUDENT); //To automatically assign a new user as a student, subject to change
        this.accountCreatedTime = accountCreatedTime;

        LoginService encryptor = new LoginService();

        this.salt = encryptor.getNewSalt();
        this.pwhash = encryptor.getHash(password, salt);
        this.imagePath = "/profile/default.png";
    }


    /**
     * Constructor to explicitly set all properties of the new object. Unlike the other constructor, accepts a value for
     * pwhash and salt instead of generating them.
     * @param pwhash The base64 encoded password hash
     * @param accountCreatedTime the time the account was created
     * @param salt The salt used to generate the hash
     */
    public User(String username, String pwhash, String firstName, String middleName, String lastName, String nickname, String bio, String pronouns, String email, Timestamp accountCreatedTime, String salt) {
        this.username = username;
        this.pwhash = pwhash;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.bio = bio;
        this.pronouns = pronouns;
        this.email = email;
        this.accountCreatedTime = accountCreatedTime;
        this.salt = salt;
        this.roles.add(UserRole.STUDENT); //To automatically assign a new user as a student, subject to change
    }


    @Override
    public String toString() {
        return "User [" + username + " (" + firstName + " " + lastName + ")]";
    }


    public int getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }


    public String getPwhash() {
        return pwhash;
    }


    public String getFirstName() {
        return firstName;
    }


    public String getMiddleName() {
        return middleName;
    }


    public String getLastName() {
        return lastName;
    }


    public String getNickname() {
        return nickname;
    }


    public String getBio() {
        return bio;
    }


    public String getPronouns() {
        return pronouns;
    }


    public String getEmail() {
        return email;
    }


    public String getSalt() {
        return salt;
    }


    public ArrayList<UserRole> getRoles() { return roles; }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }


    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public void setBio(String bio) {
        this.bio = bio;
    }


    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public void setPwhash(String password) {
        LoginService encryptor = new LoginService();
        this.pwhash = encryptor.getHash(password, salt);
    }


    public Timestamp getAccountCreatedTime() {
        return accountCreatedTime;
    }


    public void setRoles(ArrayList<UserRole> roles) {
        for (UserRole role : roles) {
            addRole(role);
        }
        for (UserRole role : getRoles()) {
            if (!roles.contains(role)) {
                deleteRole(role);
            }
        }
    }


    public void addRole(UserRole role) {
        if (! roles.contains(role)) {
            roles.add(role);
        }
    }

    /**
     * Deletes the given role from the user
     * @param role The role you want to delete from the user
     * @throws IllegalStateException If the user has 1 or less roles, we cannot delete their role(s). This is because
     * a user should never have 0 roles - therefore we're in an illegal state.
     */
    public void deleteRole(UserRole role) throws IllegalStateException {
        if (roles.size() <= 1) {
            throw new IllegalStateException("You can't have a user with 0 Roles!");
        } else {
            roles.remove(role);
        }
    }

    public String getProfileImagePath() {
        return imagePath;
    }

    public boolean deleteProfileImage() {
        File image = new File("src/main/resources/profile-photos/" + id + ".jpg");
        imagePath = "profile/default.png";
        return image.delete();
    }

    public void setProfileImagePath(String path) {
        imagePath = path;
    }
}
