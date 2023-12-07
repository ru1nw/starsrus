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
    public String addInterest() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT aid, AVG(closingbalance)*(" +
                        "SELECT floatValue FROM Settings WHERE key = 'MonthlyInterestRate'" +
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
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    public void accrueInterest(String aid, String interest) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Accounts " +
                    "SET balance = ((SELECT balance FROM Accounts WHERE aid = " + aid + ") + " + interest + ") " +
                    "WHERE aid = " + aid
                )
            ) {}
        } catch (Exception e) {
            System.err.println(e);
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
        } catch (Exception e) {
            System.err.println(e);
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
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    // 2 generate monthly statement
    public String getStatement(String username) throws SQLException {return "";}

    // 3 list active customer
    public String getActiveCustomer() throws SQLException {return "";}

    // 4 generate DTER
    public String getDTER() throws SQLException {return "";}

    // 5 customer report
    public String getCustomerReport(String username) throws SQLException {return "";}

    // 6 delete transactions
    public String deleteTransactions() throws SQLException {return "";}
}
