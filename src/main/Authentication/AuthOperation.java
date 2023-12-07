package main.Authentication;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.OracleConnection;

import main.Template.UserOperation;

public class AuthOperation extends UserOperation {
    /*
    1. Customer sign in
    2. Customer register
    3. Manager sign in
    */

    public AuthOperation(OracleConnection connection) {
        super(connection);
    }

    public final String signInCustomer(String username, String password) throws SQLException {
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
        }
    }

    public final String signUpCustomer(String name, String username, String password,
                                        String state, String phone, String email, String taxid) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO Customers (name, username, password, state, phone, email, taxid) " +
                    "VALUES ('" + name + "', '" + username + "', '" + password + "', '" +
                    state + "', '" + phone + "', '" + email + "', '" + taxid + "')"
                )
            ) {}

            Integer aid = getNextID("Accounts", "aid", statement);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO Accounts (aid, balance, uname) " +
                    "VALUES (" + aid + ", 1000, '" + username + "')" // initial deposit of $1000 in market account
                )
            ) {}

            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO MarketAccounts (aid) " +
                    "VALUES (" + aid + ")"
                )
            ) {}
        }
        return signInCustomer(username, password);
    }

    public final String signInManager(String username, String password) throws SQLException {
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
        }
    }
}
