package nz.ac.canterbury.seng302.portfolio.DTO;

/**
 * A basic login entity that we create and populate in the controller
 * in order to log in
 */
public class Login {
    private String username;
    private String password;


    public Login() {
        super();
    }

    public Login(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
