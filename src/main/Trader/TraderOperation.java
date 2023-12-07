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
            Integer marketAID = this.getMarketAccount(username, statement);

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
                    "WHERE A.aid = " + marketAID
                )
            ) {}

            Integer tid = getNextID("Transactions", "tid", statement);
            Date currentDate = getCurrentDate(statement);

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

    // 3 buy stocks
    public void buyStocks(String username, String stockSymbol, Double amount) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            Integer marketAID = this.getMarketAccount(username, statement);
            Integer stockAID = this.getOrCreateStockAccount(username, stockSymbol, statement);

            try (
                ResultSet priceSet = statement.executeQuery(
                    "SELECT price " +
                    "FROM StarStocks S " +
                    "WHERE S.symbol = '" + stockSymbol + "'"
                )
            ) {
                priceSet.next();
                Double currentPrice = priceSet.getDouble("price");

                try (
                    ResultSet resultSet = statement.executeQuery(
                        "UPDATE Accounts A " +
                        "SET A.balance = A.balance - " + currentPrice*amount + " " +
                        "WHERE A.aid = " + marketAID
                    )
                ) {}

                try (
                    ResultSet resultSet = statement.executeQuery(
                        "UPDATE Accounts A " +
                        "SET A.balance = A.balance + " + amount + " " +
                        "WHERE A.aid = " + stockAID
                    )
                ) {}

                Integer buyTid = getNextID("Transactions", "tid", statement);
                Date currentDate = getCurrentDate(statement);

                try (
                    ResultSet resultSet = statement.executeQuery(
                        "INSERT " +
                        "INTO Transactions T (tid, aid, tdate) " +
                        "VALUES (" + buyTid + ", " + stockAID + ", DATE '" + currentDate + "')"
                    )
                ) {}

                try (
                    ResultSet resultSet = statement.executeQuery(
                        "INSERT " +
                        "INTO BUYS (tid, amt, price) " +
                        "VALUES (" + buyTid + ", " + amount + ", " + currentPrice + ")"
                    )
                ) {}
            }
        }
    }

    // 6 market account balance
    public Double getCurrentBalance(String username) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            Integer marketAID = this.getMarketAccount(username, statement);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT balance " +
                    "FROM Accounts A " +
                    "WHERE A.aid = " + marketAID
                )
            ) {
                resultSet.next();
                return resultSet.getDouble("balance");
            }
        }
    }

    // 7 stock account transaction history
    public String getTransactionHistory(String username) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                // stock accounts can only buy and sell
                ResultSet resultSet = statement.executeQuery(
                    "SELECT tid, aid, TO_CHAR(tdate, 'YYYY-MM-DD') AS tdate, amt, price, ssymbol, 'buy' AS op " +
                    "FROM Transactions NATURAL JOIN Buys NATURAL JOIN StockAccounts " + 
                    "WHERE aid IN (SELECT aid " +
                    "FROM Accounts NATURAL JOIN StockAccounts " +
                    "WHERE uname = '" + username + "')" +
                    "UNION " + 
                    "SELECT tid, aid, TO_CHAR(tdate, 'YYYY-MM-DD') AS tdate, amt, sprice as price, ssymbol, 'sell' AS op " +
                    "FROM Transactions NATURAL JOIN Sells NATURAL JOIN StockAccounts " + 
                    "WHERE aid IN (SELECT aid " +
                    "FROM Accounts NATURAL JOIN StockAccounts " +
                    "WHERE uname = '" + username + "')"
                )
            ) {
                StringBuilder transactionHistory = new StringBuilder("Date\t\tTransaction Type\tStock Symbol\t# of Shares\tPrice (per share)\n");
                while (resultSet.next()) {
                    transactionHistory
                            .append(resultSet.getString("tdate")).append("\t")
                            .append(resultSet.getString("op")).append("\t\t\t")
                            .append(resultSet.getString("ssymbol")).append("\t\t")
                            .append(resultSet.getString("amt")).append("\t\t")
                            .append(resultSet.getString("price")).append("\n");
                }

                return transactionHistory.toString();
            }
        }
    }

    // 8 current price of a stock and the actor profile
    public void getStarStocks() throws SQLException {
        try (Statement statement = connection.createStatement()) {

            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT symbol, price, name, TO_CHAR(dob, 'YYYY-MM-DD') AS dob " +
                    "FROM StarStocks"
                )
            ) {
                System.out.println("Actor Name\t\tActor Date of Birth\tStock Symbol\tPrice");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("name") + "\t"
                        + resultSet.getString("dob") + "\t\t"
                        + resultSet.getString("symbol") + "\t\t"
                        + resultSet.getString("price")
                    );
                }
            }
        }
    }

    private Integer getMarketAccount(String username, Statement statement) throws SQLException {
        try (
            ResultSet resultSet = statement.executeQuery(
            "SELECT aid " +
                "FROM MarketAccounts NATURAL JOIN Accounts " +
                "WHERE uname = '" + username + "'"
            )
        ) {
            resultSet.next();
            return resultSet.getInt("aid");
        }
    }

    private Integer getOrCreateStockAccount(String username, String stockSymbol, Statement statement) throws SQLException {
        try (
            ResultSet countResultSet = statement.executeQuery(
            "SELECT COUNT(aid) as count " +
                "FROM StockAccounts NATURAL JOIN Accounts " +
                "WHERE uname = '" + username + "' AND ssymbol = '" + stockSymbol + "'"
            )
        ) {
            countResultSet.next();
            Integer count = countResultSet.getInt("count");

            if (count >= 1) {
                try (
                    ResultSet resultSet = statement.executeQuery(
                    "SELECT aid " +
                        "FROM StockAccounts NATURAL JOIN Accounts " +
                        "WHERE uname = '" + username + "' AND ssymbol = '" + stockSymbol + "'"
                    )
                ) {
                    resultSet.next();
                    return resultSet.getInt("aid");
                }
            } else {
                Integer aid = getNextID("Accounts", "aid", statement);

                try (
                    ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                        "INTO Accounts (aid, balance, uname) " +
                        "VALUES (" + aid + ", 0, '" + username + "')"
                    )
                ) {}

                try (
                    ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                        "INTO StockAccounts (aid, ssymbol) " +
                        "VALUES (" + aid + ", '" + stockSymbol + "')"
                    )
                ) {}

                return aid;
            }
        }
    }
}
