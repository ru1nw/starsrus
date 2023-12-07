package main.Manager;

import java.sql.SQLException;

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
    public String addInterest() throws SQLException {return "";}

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
