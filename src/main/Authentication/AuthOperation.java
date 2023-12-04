package main.Authentication;

import java.sql.ResultSet;
import java.sql.Statement;

import main.Template.UserOperation;
import oracle.jdbc.OracleConnection;

public class AuthOperation extends UserOperation {
    /*
    1. Customer sign in
    2. Customer register
    3. Manager sign in
    */

    public AuthOperation(OracleConnection connection) {
        super(connection);
    }

    public final String signInCustomer(String username, String password) {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT username " +
                    "FROM Customers C " +
                    "WHERE C.username = '" + username + "' " +
                    "AND C.password = '" + password + "'"
                )
            ) {
                resultSet.next();
                return resultSet.getString("username");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return "";
    }

    public final String signUpCustomer(String name, String username, String password,
                                              String state, String phone, String email, String taxid) {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO Customers (name, username, password, state, phone, email, taxid) " +
                    "VALUES ('" + name + "', '" + username + "', '" + password + "', '" +
                    state + "', '" + phone + "', '" + email + "', '" + taxid + "')"
                )
            ) {}
        } catch (Exception e) {
            System.err.println(e);
            return "";
        }
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT username " +
                    "FROM Customers C " +
                    "WHERE C.username = '" + username + "' " +
                    "AND C.password = '" + password + "'"
                )
            ) {
                resultSet.next();
                return resultSet.getString("username");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return "";
    }

    public final String signInManager(String username, String password) {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT username " +
                    "FROM Managers M " +
                    "WHERE M.username = '" + username + "' " +
                    "AND M.password = '" + password + "'"
                )
            ) {
                resultSet.next();
                return resultSet.getString("username");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return "";
    }
}
