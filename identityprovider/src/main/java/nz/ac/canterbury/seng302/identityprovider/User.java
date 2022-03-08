package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.service.PasswordEncryptorService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

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
    private ArrayList<UserRole> roles = new ArrayList<>();


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
     */
    public User(String username, String password, String firstName, String middleName, String lastName, String nickname, String bio, String pronouns, String email) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.username = username;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.bio = bio;
        this.pronouns = pronouns;
        this.email = email;
        this.roles.add(UserRole.STUDENT); //To automatically assign a new user as a student, subject to change

        PasswordEncryptorService encryptor = new PasswordEncryptorService();

        this.salt = encryptor.getNewSalt();
        this.pwhash = encryptor.getHash(password, salt);

    }

    /**
     * Constructor to explicitly set all properties of the new object. Unlike the other constructor, accepts a value for
     * pwhash and salt instead of generating them.
     * @param pwhash The base64 encoded password hash
     * @param salt The salt used to generate the hash
     */
    public User(String username, String pwhash, String firstName, String middleName, String lastName, String nickname, String bio, String pronouns, String email, String salt) {
        this.username = username;
        this.pwhash = pwhash;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.bio = bio;
        this.pronouns = pronouns;
        this.email = email;
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

    public void setPwhash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PasswordEncryptorService encryptor = new PasswordEncryptorService();

        this.pwhash = encryptor.getHash(password, salt);
    }
}
