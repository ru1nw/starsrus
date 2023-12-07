package main.Trader;

import main.Template.UserOperation;
import oracle.jdbc.OracleConnection;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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
                System.err.println(e);
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

            if (transactionTable == null) return;

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
    public Double buyStocks(String username, String stockSymbol, Double amount) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            Integer marketAID = this.getMarketAccount(username, statement);
            Integer stockAID = this.getOrCreateStockAccount(username, stockSymbol, statement);

            Double currentPrice = getStockPrice(stockSymbol, statement);
            Double totalCost = currentPrice*amount+20; // included $20 transaction fee

            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Accounts A " +
                    "SET A.balance = A.balance - " + totalCost + " " +
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

            return totalCost;
        }
    }

    // 4 sell stocks
    public Double sellStocks(String username, String stockSymbol, Double buyPrice, Double amount) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            Integer marketAID = this.getMarketAccount(username, statement);
            Integer stockAID = this.getOrCreateStockAccount(username, stockSymbol, statement);

            Double currentPrice = getStockPrice(stockSymbol, statement);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Accounts A " +
                    "SET A.balance = A.balance + " + (currentPrice*amount-20) + " " +
                    "WHERE A.aid = " + marketAID
                )
            ) {}

            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Accounts A " +
                    "SET A.balance = A.balance - " + amount + " " +
                    "WHERE A.aid = " + stockAID
                )
            ) {}

            Integer sellTid = getNextID("Transactions", "tid", statement);
            Date currentDate = getCurrentDate(statement);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO Transactions T (tid, aid, tdate) " +
                    "VALUES (" + sellTid + ", " + stockAID + ", DATE '" + currentDate + "')"
                )
            ) {}

            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO SELLS (tid, amt, sprice, bprice) " +
                    "VALUES (" + sellTid + ", " + amount + ", " + currentPrice + ", " + buyPrice + ")"
                )
            ) {}

            return (currentPrice-buyPrice)*amount-20;
        }
    }

    static class OwnedShare {
        Double price;
        Double amount;

        public OwnedShare(Double price, Double amount) {
            this.price = price;
            this.amount = amount;
        }
    }

    public ArrayList<OwnedShare> getOwnedShares(String username, String stockSymbol) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            Integer stockAID = this.getOrCreateStockAccount(username, stockSymbol, statement);

            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT B.buyPrice as price, (B.buyAmt-coalesce(S.sellAmt, 0)) as amt FROM " +
                    "(SELECT SUM(amt) as buyAmt, price as buyPrice " +
                    "FROM Buys NATURAL JOIN Transactions " +
                    "WHERE aid = " + stockAID + " AND tid NOT IN (SELECT target as tid from Cancels)" +
                    "GROUP BY price) B " +
                    "FULL JOIN " +
                    "(SELECT SUM(amt) as sellAmt, bprice as buyPrice " +
                    "FROM Sells NATURAL JOIN Transactions " +
                    "WHERE aid = " + stockAID + " AND tid NOT IN (SELECT target as tid from Cancels) " +
                    "GROUP BY bprice) S " +
                    "ON B.buyPrice = S.buyPrice"
                )
            ) {
                ArrayList<OwnedShare> ownedShares = new ArrayList<>();
                while (resultSet.next()) {
                    ownedShares.add(new OwnedShare(resultSet.getDouble("price"), resultSet.getDouble("amt")));
                }

                return ownedShares;
            }
        }
    }

    private Double getStockPrice(String stockSymbol, Statement statement) throws SQLException {
        try (
            ResultSet priceSet = statement.executeQuery(
                "SELECT price " +
                "FROM StarStocks S " +
                "WHERE S.symbol = '" + stockSymbol + "'"
            )
        ) {
            priceSet.next();
            return priceSet.getDouble("price");
        }
    }

    // 5 cancel last (buy/sell) transaction
    public String cancelTransaction(String username) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // TODO: Switch back
            Date currentDate = getCurrentDate(statement);
//            Date currentDate = new Date(2023-1900, 10-1, 18);

            Integer lastTID = null;
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT tid as lastTID " +
                    "FROM (SELECT MAX(tid) as tid " +
                    "FROM Transactions NATURAL JOIN Accounts " +
                    "WHERE uname = '" + username + "' AND tdate = DATE '" + currentDate + "') " +
                    "WHERE tid IN (SELECT tid FROM Buys) OR tid IN (SELECT tid FROM Sells)"
                )
            ) {
                while (resultSet.next()) {
                    lastTID = resultSet.getInt("lastTID");
                }
            }

            if (lastTID == null) return null;

            Integer marketAID = this.getMarketAccount(username, statement);

            Integer stockAID;
            String stockSymbol;
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT aid, ssymbol " +
                    "FROM Transactions NATURAL JOIN Accounts NATURAL JOIN StockAccounts " +
                    "WHERE tid = " + lastTID
                )
            ) {
                resultSet.next();
                stockAID = resultSet.getInt("aid");
                stockSymbol = resultSet.getString("ssymbol");
            }

            String response = null;

            try (
                ResultSet sellSet = statement.executeQuery(
                    "SELECT amt, sprice " +
                    "FROM Sells " +
                    "WHERE tid = " + lastTID
                )
            ) {
                sellSet.next();
                Double amount = sellSet.getDouble("amt");
                Double sellPrice = sellSet.getDouble("sprice");

                Double costAmount = amount*sellPrice+20;

                try (
                    ResultSet resultSet = statement.executeQuery(
                        "UPDATE Accounts A " +
                        "SET A.balance = A.balance - " + costAmount + " " +
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

                response = "Successfully canceled selling " + amount + " shares of " + stockSymbol + " for a total value of $" + costAmount + " (including fee)";
            } catch (SQLException e) {
                switch (e.getErrorCode()) {
                    case 17289: // result set is empty
                        break;
                    default:
                        throw e;
                }
            }

            try (
                ResultSet buySet = statement.executeQuery(
                    "SELECT amt, price " +
                    "FROM Buys " +
                    "WHERE tid = " + lastTID
                )
            ) {
                buySet.next();
                Double amount = buySet.getDouble("amt");
                Double price = buySet.getDouble("price");

                Double refundAmount = (amount*price-20);

                try (
                    ResultSet resultSet = statement.executeQuery(
                        "UPDATE Accounts A " +
                        "SET A.balance = A.balance + " + refundAmount + " " +
                        "WHERE A.aid = " + marketAID
                    )
                ) {}

                try (
                    ResultSet resultSet = statement.executeQuery(
                        "UPDATE Accounts A " +
                        "SET A.balance = A.balance - " + amount + " " +
                        "WHERE A.aid = " + stockAID
                    )
                ) {}

                response = "Successfully canceled buying " + amount + " shares of " + stockSymbol + " for a total value of $" + refundAmount + " (including fee)";
            } catch (SQLException e) {
                switch (e.getErrorCode()) {
                    case 17289: // result set is empty
                        break;
                    default:
                        throw e;
                }
            }

            Integer cancelTID = getNextID("Transactions", "tid", statement);
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO Transactions T (tid, aid, tdate) " +
                    "VALUES (" + cancelTID + ", " + stockAID + ", DATE '" + currentDate + "')"
                )
            ) {}

            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT " +
                    "INTO Cancels T (tid, target) " +
                    "VALUES (" + cancelTID + ", " + lastTID + ")"
                )
            ) {}

            return response;
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
                    "SELECT * FROM (" +
                    "SELECT tid, aid, TO_CHAR(tdate, 'YYYY-MM-DD') AS tdate, amt, price, ssymbol, 'buy' AS op " +
                    "FROM Transactions NATURAL JOIN Buys NATURAL JOIN StockAccounts " + 
                    "WHERE aid IN (SELECT aid " +
                    "FROM Accounts NATURAL JOIN StockAccounts " +
                    "WHERE uname = '" + username + "') " +
                    "UNION " + 
                    "SELECT tid, aid, TO_CHAR(tdate, 'YYYY-MM-DD') AS tdate, amt, sprice as price, ssymbol, 'sell' AS op " +
                    "FROM Transactions NATURAL JOIN Sells NATURAL JOIN StockAccounts " + 
                    "WHERE aid IN (SELECT aid " +
                    "FROM Accounts NATURAL JOIN StockAccounts " +
                    "WHERE uname = '" + username + "') " +
                    "UNION " +
                    "SELECT tid, aid, TO_CHAR(tdate, 'YYYY-MM-DD') AS tdate, null as amt, null as price, ssymbol, 'cancel' AS op " +
                    "FROM Transactions NATURAL JOIN Cancels NATURAL JOIN " +
                    "(SELECT tid as target, ssymbol FROM Transactions NATURAL JOIN StockAccounts) " +
                    "WHERE aid IN (SELECT aid " +
                    "FROM Accounts NATURAL JOIN StockAccounts " +
                    "WHERE uname = '" + username + "') " +
                    ") ORDER BY tdate DESC, tid DESC"
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
    public String getAllStocks() throws SQLException {
        try (Statement statement = connection.createStatement()) {

            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT symbol, price, name, TO_CHAR(dob, 'YYYY-MM-DD') AS dob " +
                    "FROM StarStocks"
                )
            ) {
                StringBuilder transactionHistory = new StringBuilder("Stock Symbol\tActor Name\t\tActor DOB\tPrice\n");
                while (resultSet.next()) {
                    transactionHistory
                            .append(resultSet.getString("symbol")).append("\t\t")
                            .append(resultSet.getString("name")).append("\t")
                            .append(resultSet.getString("dob")).append("\t")
                            .append(resultSet.getString("price")).append("\n");
                }

                return transactionHistory.toString();
            }
        }
    }

    // 9 list movie information
    // see src/main/Movie

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

    public ArrayList<String> getStockAccountSymbols(String username) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT ssymbol " +
                    "FROM StockAccounts NATURAL JOIN Accounts " +
                    "WHERE uname = '" + username + "' AND balance > 0"
                )
            ) {
                ArrayList<String> symbols = new ArrayList<>();
                while (resultSet.next()) {
                    symbols.add(resultSet.getString("ssymbol"));
                }

                return symbols;
            }
        }
    }
}
