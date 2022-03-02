package nz.ac.canterbury.seng302.identityprovider.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseService {

    public static Connection conn;

    public static void setUpDatabase() throws SQLException {

        String url = "jdbc:h2:mem:";
        Connection conn = DriverManager.getConnection(url);
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


    public static void main(String[] args) {
        Connection conn = null;
        try {
            String url = "jdbc:h2:mem:";
            conn = DriverManager.getConnection(url);
            Statement statement = conn.createStatement();
            String sql = "create table if not exists ftest (\n" +
                    "    id int,\n" +
                    "    name varchar(20)\n" +
                    ");";

            statement.executeUpdate(sql);

            sql = "insert into ftest values (10, 'Freddy');";
            statement.executeUpdate(sql);

            ResultSet result = conn.createStatement().executeQuery("SELECT * FROM ftest");
            while (result.next()) {
                System.out.print(result.getInt("id"));
                System.out.print(" ");
                System.out.println(result.getString("name"));
            }


        } catch (SQLException e) {
            throw new Error("Problem", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

}
