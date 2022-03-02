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

    }

    public static void addUsertoDatabase(User user) throws SQLException {
        String sql = "INSERT INTO USERS VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement ps = conn.prepareStatement(sql);

        int i = 0;
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
        ResultSet result = ps.executeQuery();

        if (result.next()) {
            return new User(
                result.getString(0),
                result.getString(1),
                result.getString(2),
                result.getString(3),
                result.getString(4),
                result.getString(5),
                result.getString(6),
                result.getString(7)
            );
        } else {
            return null;
        }


    }

}
