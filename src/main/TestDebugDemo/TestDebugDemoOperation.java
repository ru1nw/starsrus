package main.TestDebugDemo;

import oracle.jdbc.OracleConnection;

import main.Template.UserOperation;

public class TestDebugDemoOperation extends UserOperation {
    /*
    1. Open Market for the Day
    2. Close Market for the Day
    3. Set a new price for a stock
    4. Set a new date to be today's date
    0. Back to Authentication
    */

    public TestDebugDemoOperation(OracleConnection connection) {
        super(connection);
    }
}
