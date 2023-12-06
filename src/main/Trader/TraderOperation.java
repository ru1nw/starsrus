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

    // 1 deposit
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

    // 2 withdrawal
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

    // 6 market account balance
    public Double getCurrentBalance(String username) throws SQLException {
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

    // 7 stock account transaction history
    public void getTransactionHistory(String username) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String marketAID = this.getMarketAccount(username, statement);

            try (
                // stock accounts can only buy and sell
                ResultSet resultSet = statement.executeQuery(
                    "SELECT tid, aid, TO_CHAR(tdate, 'YYYY-MM-DD') AS tdate, amt, price, ssymbol, 'buy' AS op " +
                    "FROM Transactions NATURAL JOIN Buys NATURAL JOIN StockAccounts " + 
                    "WHERE aid IN (SELECT aid " +
                    "FROM Accounts NATURAL JOIN StockAccounts " +
                    "WHERE uname = '" + username + "')" +
                    "UNION " + 
                    "SELECT tid, aid, TO_CHAR(tdate, 'YYYY-MM-DD') AS tdate, amt, price, ssymbol, 'sell' AS op " +
                    "FROM Transactions NATURAL JOIN Sells NATURAL JOIN StockAccounts " + 
                    "WHERE aid IN (SELECT aid " +
                    "FROM Accounts NATURAL JOIN StockAccounts " +
                    "WHERE uname = '" + username + "')"
                )
            ) {
                System.out.println("Date\t\tTransaction Type\tStock Symbol\t# of Shares\tPrice");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("tdate") + "\t"
                        + resultSet.getString("op") + "\t\t\t"
                        + resultSet.getString("ssymbol") + "\t\t"
                        + resultSet.getString("amt") + "\t\t"
                        + resultSet.getString("price")
                    );
                }
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
