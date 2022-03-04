package nz.ac.canterbury.seng302.identityprovider;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true)
    private String username;

    private String password;
    private String firstName;
    private String lastName;
    private String nickname;
    private String bio;
    private String pronouns;
    private String email;

    protected User () {} // Used by JPA

    public User(String username, String password, String firstName, String lastName, String nickname, String bio, String pronouns, String email) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.bio = bio;
        this.pronouns = pronouns;
        this.email = email;
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

    public String getPassword() {
        return password;
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

    public String getFullName() {return username + " " + lastName;}
}
