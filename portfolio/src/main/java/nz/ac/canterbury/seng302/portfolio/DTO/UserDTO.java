package nz.ac.canterbury.seng302.portfolio.DTO;

import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;

public class UserDTO {

    private final int id;
    private final String username;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String nickname;
    private final String bio;
    private final String pronouns;
    private final String email;
    private final String imagePath;

    public UserDTO(UserResponse userResponse) {
        this.id = userResponse.getId();
        this.username = userResponse.getUsername();
        this.firstName = userResponse.getFirstName();
        this.middleName = userResponse.getMiddleName();
        this.lastName = userResponse.getLastName();
        this.nickname = userResponse.getNickname();
        this.bio = userResponse.getBio();
        this.pronouns = userResponse.getPersonalPronouns();
        this.email = userResponse.getEmail();
        this.imagePath = userResponse.getProfileImagePath();
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
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

    public String getImagePath() {
        return imagePath;
    }
}
