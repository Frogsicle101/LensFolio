package nz.ac.canterbury.seng302.identityprovider;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.identityprovider.service.LoginService;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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

    @Column(length = 100000)
    private Timestamp accountCreatedTime;
    private String salt;

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
        this.accountCreatedTime = accountCreatedTime;

        LoginService encryptor = new LoginService();

        this.salt = encryptor.getNewSalt();
        this.pwhash = encryptor.getHash(password, salt);

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
}
