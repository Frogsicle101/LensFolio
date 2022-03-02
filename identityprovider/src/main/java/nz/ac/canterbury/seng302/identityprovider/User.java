package nz.ac.canterbury.seng302.identityprovider;

public class User {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String nickname;
    private String bio;
    private String pronouns;
    private String email;

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
}
