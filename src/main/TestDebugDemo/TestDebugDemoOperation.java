package main.TestDebugDemo;

import oracle.jdbc.OracleConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    
    public void toggleMarket(String val) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Settings " +
                    "SET boolvalue = " + val + " " +
                    "WHERE key = 'isMarketOpen'"
                )
            ) {}
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    // 1 open market
    public void openMarket() throws SQLException {
        toggleMarket("1");
    }

    // 2 close market
    public void closeMarket() throws SQLException {
        toggleMarket("0");
    }

    // 3 new stock price
    public void setStockPrice(String stock, String price) throws SQLException {
        stock = stock.toUpperCase();
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE StarStocks " +
                    "SET price = " + price + " " +
                    "WHERE symbol = '" + stock + "'"
                )
            ) {}
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    // 4 set date
    public void setDate(String date) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Settings " +
                    "SET datevalue = DATE '" + date + "' " +
                    "WHERE key = 'currentDate'"
                )
            ) {}
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
