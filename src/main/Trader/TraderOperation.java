package main.Trader;

import main.Template.UserOperation;
import oracle.jdbc.OracleConnection;

import java.sql.Date;
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
        updateBalance(username, amount, "Deposits");
    }

    // 2 withdrawal
    public void withdrawFunds(String username, Double amount) throws SQLException {
        updateBalance(username, -1*amount, "Withdraws");
    }

    private void updateBalance(String username, Double amountDifference, String transactionTable) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String marketAID = this.getMarketAccount(username, statement);

            Double currentBalance;
            try {
                currentBalance = getCurrentBalance(username);
            } catch (SQLException e) {
                System.out.println(e);
                return;
            }
            
            Double newBalance = currentBalance + amountDifference;
            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Accounts A " +
                    "SET A.balance = " + newBalance + " " +
                    "WHERE A.aid = '" + marketAID + "'"
                )
            ) {}

            Integer tid = getNextID("Transactions", "tid");

            // TODO: Fetch current date from database somewhere
            Date currentDate = new Date(2023-1900, 12-1, 6);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO Transactions T (tid, aid, tdate) " +
                    "VALUES (" + tid + ", " + marketAID + ", DATE '" + currentDate + "')"
                )
            ) {}

            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO " + transactionTable + " T (tid, amt, result) " +
                    "VALUES (" + tid + ", " + Math.abs(amountDifference) + ", " + newBalance + ")"
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
