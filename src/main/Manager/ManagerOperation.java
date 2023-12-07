package main.Manager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.OracleConnection;

import main.Template.UserOperation;

public class ManagerOperation extends UserOperation {
    /*
    1. Add Interest
    2. Generate Monthly Statement
    3. List Active Customers
    4. Generate Government Drug & Tax Evasion Report (DTER)
    5. Customer Report
    6. Delete Transactions
    0. Logout
    */

    public ManagerOperation(OracleConnection connection) {
        super(connection);
    }

    // 1 add interest
    public final String addInterest() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT aid, AVG(closingbalance)*(" +
                        "SELECT floatValue FROM Settings WHERE key = 'monthlyInterestRate'" +
                    ") AS interest " +
                    "FROM DailyClosingBalances " +
                    "GROUP BY aid"
                )
            ) {
                StringBuilder transactionHistory = new StringBuilder("aid\tinterest\n");
                while (resultSet.next()) {
                    String aid = resultSet.getString("aid");
                    String interest = resultSet.getString("interest");
                    accrueInterest(aid, interest);
                    transactionHistory.append(aid + "\t" + interest);
                }
                return transactionHistory.toString();
            }
        }
    }

    public final void accrueInterest(String aid, String interest) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Accounts " +
                    "SET balance = ((SELECT balance FROM Accounts WHERE aid = " + aid + ") + " + interest + ") " +
                    "WHERE aid = " + aid
                )
            ) {}
        }
        
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT INTO Transactions (tid, aid, tdate) " +
                    "VALUES (" +
                        "(SELECT MAX(tid)+1 FROM Transactions), " +
                        aid + ", " +
                        "(SELECT dateValue FROM Settings WHERE key = 'currentDate')" +
                    ")"
                )
            ) {}
        }
        
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT INTO AccrueInterests (tid, amt, result) " +
                    "VALUES (" +
                        "(SELECT MAX(tid) FROM Transactions), " +
                        interest + ", " +
                        "(SELECT balance FROM Accounts WHERE aid = " + aid + ")" +
                    ")"
                )
            ) {}
        }
    }

    // 2 generate monthly statement
    public final String getStatement(String username) throws SQLException {
        StringBuilder transactionHistory = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT name, email " +
                    "FROM Customers " +
                    "WHERE username = '" + username + "'"
                )
            ) {
                transactionHistory.append("Name\t\t\t\tEmail\n");
                resultSet.next();
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                transactionHistory.append(name + email + "\n");
            }
        }
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT " +
                        "D1.closingDate AS initDate, " +
                        "D1.closingBalance AS initBalance, " +
                        "D2.closingDate AS endDate, " +
                        "D2.closingBalance AS endBalance " +
                    "FROM DailyClosingBalances D1, DailyClosingBalances D2 " +
                    "WHERE D1.aid IN (" +
                        "SELECT aid " +
                        "FROM Accounts " +
                            "JOIN Customers ON Accounts.uname=Customers.username " +
                        "WHERE uname = '" + username + "'" +
                    ") AND D1.aid=D2.aid AND D1.closingDate = (" +
                        "SELECT MIN(closingDate) FROM DailyClosingBalances D3 WHERE D1.aid=D3.aid" +
                    ") AND D2.closingDate = (" +
                        "SELECT MAX(closingDate) FROM DailyClosingBalances D3 WHERE D2.aid=D3.aid" +
                    ")"
                )
            ) {
                transactionHistory.append("Start Date\tStart Balance\tEnd Date\tEnd Balance\n");
                while (resultSet.next()){
                    String initDate = resultSet.getString("initDate").substring(0, 11);
                    String initBalance = resultSet.getString("initBalance");
                    String endDate = resultSet.getString("endDate").substring(0, 11);
                    String endBalance = resultSet.getString("endBalance");
                    transactionHistory.append(initDate + "\t" + initBalance + "\t\t" + endDate + "\t" + endBalance + "\n");
                }
            }
        }
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT sumEndBalance-sumInitBalance-bCount*20-sCount*20-cCount*20 AS profit " +
                    "FROM (" +
                        "SELECT SUM(D1.closingBalance) AS sumInitBalance, SUM(D2.closingBalance) AS sumEndBalance " +
                        "FROM DailyClosingBalances D1, DailyClosingBalances D2 " +
                        "WHERE D1.aid IN (" +
                            "SELECT aid " +
                            "FROM Accounts " +
                                "JOIN Customers ON Accounts.uname=Customers.username " +
                            "WHERE uname = '" + username + "'" +
                        ") AND D1.aid=D2.aid AND D1.closingDate = (" +
                            "SELECT MIN(closingDate) FROM DailyClosingBalances D3 WHERE D1.aid=D3.aid" +
                        ") AND D2.closingDate = (" +
                            "SELECT MAX(closingDate) FROM DailyClosingBalances D3 WHERE D2.aid=D3.aid" +
                        ")" +
                    "), (" +
                        "SELECT COUNT(tid) AS bCount " +
                        "FROM Buys NATURAL JOIN Transactions " +
                        "WHERE aid IN (" +
                            "SELECT aid " +
                            "FROM Accounts " +
                                "JOIN Customers ON Accounts.uname=Customers.username " +
                            "WHERE uname = '" + username + "'" +
                        ")" +
                    "), (" +
                        "SELECT COUNT(tid) AS sCount " +
                        "FROM Sells NATURAL JOIN Transactions " +
                        "WHERE aid IN (" +
                            "SELECT aid " +
                            "FROM Accounts " +
                                "JOIN Customers ON Accounts.uname=Customers.username " +
                            "WHERE uname = '" + username + "'" +
                        ")" +
                    "), (" +
                        "SELECT COUNT(tid) AS cCount " +
                        "FROM Cancels NATURAL JOIN Transactions " +
                        "WHERE aid IN (" +
                            "SELECT aid " +
                            "FROM Accounts " +
                                "JOIN Customers ON Accounts.uname=Customers.username " +
                            "WHERE uname = '" + username + "'" +
                        ")" +
                    ")"
                )
            ) {
                transactionHistory.append("Total monthly profit: $");
                resultSet.next();
                transactionHistory.append(resultSet.getString("profit") + "\n");
            }
        }
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT aid, tdate " +
                    "FROM Transactions " +
                    "WHERE aid IN (" +
                        "SELECT aid " +
                        "FROM Accounts " +
                            "JOIN Customers ON Accounts.uname=Customers.username " +
                        "WHERE uname = '" + username + "'" +
                    ")"
                )
            ) {
                transactionHistory.append("Transaction history:\nAccount ID\tTransaction date\n");
                while (resultSet.next()) {
                    String aid = resultSet.getString("aid");
                    String tdate = resultSet.getString("tdate");
                    transactionHistory.append(aid + "\t\t" + tdate + "\n");
                }
                return transactionHistory.toString();
            }
        }
    }

    // 3 list active customer
    public final String getActiveCustomer() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
               ResultSet resultSet = statement.executeQuery(
                    "SELECT buys+sells as shares, B.uname " +
                    "FROM (SELECT SUM(amt) as buys, uname " +
                    "FROM Buys NATURAL JOIN Transactions NATURAL JOIN Accounts " +
                    "GROUP BY uname " +
                    ") B INNER JOIN (SELECT SUM(amt) as sells, uname " +
                    "FROM Sells NATURAL JOIN Transactions NATURAL JOIN Accounts " +
                    "GROUP BY uname " +
                    ") S ON B.uname = S.uname " +
                    "WHERE buys+sells >= 1000"
                )
            ) {
                StringBuilder activeCustomers = new StringBuilder("Username\t\tTotal shares traded\n");
                while (resultSet.next()) {
                    activeCustomers
                            .append(resultSet.getString("uname")).append("\t")
                            .append(resultSet.getString("shares")).append("\n");
                }

                return activeCustomers.toString();
            }
        }
    }

    // 4 generate DTER
    public final String getDTER() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
               ResultSet resultSet = statement.executeQuery(
                    "SELECT uname, profit*(1+interest*1) as profit, state " +
                    "FROM (SELECT SUM((sprice-bprice)*amt) as profit, uname " +
                    "FROM Sells NATURAL JOIN Transactions NATURAL JOIN Accounts " +
                    "WHERE tid NOT IN (SELECT target FROM Cancels) " +
                    "GROUP BY uname) A INNER JOIN Customers C ON A.uname = C.username, " +
                    "(SELECT floatValue as interest FROM Settings WHERE key = 'monthlyInterestRate') " +
                    "WHERE profit*(1+interest*1) >= 10000"
                )
            ) {
                StringBuilder dter = new StringBuilder("Username\t\tProfit\tState\n");
                while (resultSet.next()) {
                    dter
                            .append(resultSet.getString("uname")).append("\t")
                            .append(resultSet.getString("profit")).append("\t")
                            .append(resultSet.getString("state")).append("\n");
                }

                return dter.toString();
            }
        }
    }

    // 5 customer report
    public final String getCustomerReport(String username) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
               ResultSet resultSet = statement.executeQuery(
                    "SELECT aid, type, balance " +
                    "FROM (SELECT aid, 'market acc' as type, balance " +
                    "FROM MarketAccounts NATURAL JOIN Accounts " +
                    "WHERE uname = '" + username + "' " +
                    ") UNION (SELECT aid, concat('stock ', ssymbol) as type, balance " +
                    "FROM StockAccounts NATURAL JOIN Accounts " +
                    "WHERE uname = '" + username + "')"
                )
            ) {
                StringBuilder report = new StringBuilder("Account ID\tType\t\tBalance\n");
                while (resultSet.next()) {
                    report
                            .append(resultSet.getString("aid")).append("\t\t")
                            .append(resultSet.getString("type")).append("\t")
                            .append(resultSet.getString("balance")).append("\n");
                }

                return report.toString();
            }
        }
    }

    // 6 delete transactions
    public final void deleteTransactions() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
               ResultSet resultSet = statement.executeQuery(
                    "DELETE FROM Transactions"
                )
            ) {}
        }
    }
}
