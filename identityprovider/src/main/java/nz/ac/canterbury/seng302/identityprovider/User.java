package nz.ac.canterbury.seng302.identityprovider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.identityprovider.groups.Group;
import nz.ac.canterbury.seng302.identityprovider.service.LoginService;
import nz.ac.canterbury.seng302.identityprovider.service.UrlUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.core.env.Environment;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The object used to store Users in the database
 *
 * These users have an automatically generated id which is the primary key for users in the database.
 * The attributes contained in this class reflect the attributes that would be passed/used in the user_accounts.proto
 * contract.
 */
@Entity
public class User {

    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true)
    private String username;

    /** A hash of the user's password. */
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

    @JsonIgnore
    @ManyToMany(mappedBy = "userList", fetch = FetchType.EAGER)
    private final List<Group> groups = new ArrayList<>();


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


    public List<UserRole> getRoles() { return roles; }


    public String getRolesCsv() {
        ArrayList<String> rolesStrings = new ArrayList<>();
        for (UserRole role : roles) {
            rolesStrings.add(role.toString().toLowerCase(Locale.ROOT));
        }

        return String.join(",", rolesStrings);
    }


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


    public boolean deleteProfileImage(Environment env) {
        String photoLocation = env.getProperty("photoLocation", "src/main/resources/profile-photos/");

        File image = new File(photoLocation + id + ".jpg");
        imagePath = "profile/default.png";
        return image.delete();
    }


    public void setProfileImagePath(String path) {
        imagePath = path;
    }

    public List<Group> getGroups() {
        return groups;
    }


    public UserResponse userResponse() {
        UserResponse.Builder response = UserResponse.newBuilder();
        response.setUsername(this.getUsername())
                .setFirstName(this.getFirstName())
                .setMiddleName(this.getMiddleName())
                .setLastName(this.getLastName())
                .setNickname(this.getNickname())
                .setBio(this.getBio())
                .setPersonalPronouns(this.getPronouns())
                .setEmail(this.getEmail())
                .setCreated(this.getAccountCreatedTime())
                .setId(this.getId())
                .setProfileImagePath(UrlUtil.getUrlService().getProfileURL(this).toString());

        // To add all the users roles to the response
        List<UserRole> roles = this.getRoles();
        for (UserRole role : roles) {
            response.addRoles(role);
        }

        return response.build();
    }
}
