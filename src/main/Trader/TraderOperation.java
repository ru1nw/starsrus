package main.Trader;

import main.Template.UserOperation;
import oracle.jdbc.OracleConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TraderOperation extends UserOperation {
    /*
    1. Deposit
    2. Withdrawal
    3. Buy
    4. Sell
    5. Cancel
    6. Show market account balance
    7. Show stock account transaction history
    8. List current price of a stock and the actor profile
    9. List movie information
    */

    public TraderOperation(OracleConnection connection) {
        super(connection);
    }

    public void depositFunds(String username, Double amount) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String marketAID = this.getMarketAccount(username, statement);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Accounts A " +
                    "SET A.balance = A.balance + " + amount.toString() + " " +
                    "WHERE A.aid = '" + marketAID + "'"
                )
            ) {}
        }
    }

    public void withdrawFunds(String username, Double amount) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String marketAID = this.getMarketAccount(username, statement);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Accounts A " +
                    "SET A.balance = A.balance - " + amount.toString() + " " +
                    "WHERE A.aid = '" + marketAID + "'"
                )
            ) {}
        }
    }

    public Double getCurrentFunds(String username) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String marketAID = this.getMarketAccount(username, statement);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "Select balance " +
                    "FROM Accounts A " +
                    "WHERE A.aid = '" + marketAID + "'"
                )
            ) {
                resultSet.next();
                return resultSet.getDouble("balance");
            }
        }
    }

    private String getMarketAccount(String username, Statement statement) throws SQLException {
        try (
            ResultSet resultSet = statement.executeQuery(
            "SELECT aid " +
                "FROM MarketAccounts M " +
                "WHERE M.aid IN (" +
                    "SELECT A.aid " +
                    "FROM Accounts A " +
                    "WHERE A.uname = '" + username + "'" +
                ")"
            )
        ) {
            resultSet.next();
            return resultSet.getString("aid");
        }
    }
}
