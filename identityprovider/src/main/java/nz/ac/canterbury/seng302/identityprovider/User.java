package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.service.PasswordEncryptorService;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Entity
public class User {

    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true)
    private String username;

    private String pwhash;
    private String firstName;
    private String lastName;
    private String nickname;
    private String bio;
    private String pronouns;
    private String email;
    private String salt;

    /**
     * Generic constructor used by JPA
     */
    protected User () {}

    /**
     * Constructs a new user object. Calculates and stores a hash of the given password with unique salt.
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @param nickname
     * @param bio
     * @param pronouns
     * @param email
     */
    public User(String username, String password, String firstName, String lastName, String nickname, String bio, String pronouns, String email) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.bio = bio;
        this.pronouns = pronouns;
        this.email = email;

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
    public User(String username, String pwhash, String firstName, String lastName, String nickname, String bio, String pronouns, String email, String salt) {
        this.username = username;
        this.pwhash = pwhash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.bio = bio;
        this.pronouns = pronouns;
        this.email = email;
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
}
