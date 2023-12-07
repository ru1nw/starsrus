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
    public void getStatement(String username) throws SQLException {}

    // 3 list active customer
    public void getActiveCustomer() throws SQLException {}

    // 4 generate DTER
    public void getDTER() throws SQLException {}

    // 5 customer report
    public void getCustomerReport(String username) throws SQLException {}

    // 6 delete transactions
    public void deleteTransactions() throws SQLException {}
}
