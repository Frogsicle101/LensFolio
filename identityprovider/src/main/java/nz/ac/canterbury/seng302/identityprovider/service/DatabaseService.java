package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    public static Connection conn;

    public static void setUpDatabase() throws SQLException {

        String url = "jdbc:h2:mem:";
        conn = DriverManager.getConnection(url);
        Statement statement = conn.createStatement();
        String sql = "CREATE TABLE USERS (" +
                "USERNAME VARCHAR(20) PRIMARY KEY," +
                "PASSWORD VARCHAR(20), " +
                "FIRST_NAME VARCHAR(20)," +
                "LAST_NAME VARCHAR(20)," +
                "NICKNAME VARCHAR(20)," +
                "BIO VARCHAR(140)," +
                "PRONOUNS VARCHAR(30)," +
                "EMAIL VARCHAR(30));";
        statement.executeUpdate(sql);

        DatabaseService.addTestUser(); // For testing

    }

    public static void addUsertoDatabase(User user) throws SQLException {
        String sql = "INSERT INTO USERS VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement ps = conn.prepareStatement(sql);

        int i = 1;
        ps.setString(i++, user.getUsername());
        ps.setString(i++, user.getPassword());
        ps.setString(i++, user.getFirstName());
        ps.setString(i++, user.getLastName());
        ps.setString(i++, user.getNickname());
        ps.setString(i++, user.getBio());
        ps.setString(i++, user.getPronouns());
        ps.setString(i, user.getEmail());

        ps.executeUpdate();

    }

    public static User getUserfromDatabase(String username) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE USERNAME LIKE ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ResultSet result = ps.executeQuery();

        if (result.next()) {
            return new User(

                result.getString(1),
                result.getString(2),
                result.getString(3),
                result.getString(4),
                result.getString(5),
                result.getString(6),
                result.getString(7),
                result.getString(8)
            );
        } else {
            // Throw exception for username not found
            throw new NullPointerException("Username not found");
        }
    }

    // This method is only for testing purposes and should be removed for launch
    private static void addTestUser() throws SQLException {
        try {
        DatabaseService.addUsertoDatabase(new User(
                "abc123",
                "Password123!",
                "Valid",
                "User",
                "val",
                "bio for Valid User",
                "he/him",
                "example@email.com"
        ));
        } catch (SQLException exception) {
            System.out.println("Failed to add test user to database");
        }
    }

}
