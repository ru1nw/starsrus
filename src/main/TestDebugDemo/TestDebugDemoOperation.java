package main.TestDebugDemo;

import oracle.jdbc.OracleConnection;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import main.Template.UserOperation;

public class TestDebugDemoOperation extends UserOperation {
    /*
    1. Open Market for the Day
    2. Close Market for the Day
    3. Set a new price for a stock
    4. Set a new date to be today's date
    5. Set monthly interest rate
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
        }
    }

    // 1 open market
    public void openMarket() throws SQLException {
        toggleMarket("1");
    }

    // 2 close market
    public void closeMarket() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT INTO DailyClosingBalances (aid, closingDate, closingBalance) " +
                        "SELECT aid, (SELECT dateValue FROM Settings WHERE key = 'currentDate'), balance " +
                        "FROM Accounts"
                )
            ) {}
        }
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT INTO DailyClosingPrices (ssymbol, closingDate, closingPrice) " +
                        "SELECT symbol, (SELECT dateValue FROM Settings WHERE key = 'currentDate'), price " +
                        "FROM StarStocks"
                )
            ) {}
        }
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
        }
    }

    // 4 set date
    public void setDate(String date) throws SQLException {
        Calendar dateIterator = Calendar.getInstance();
        Date oldDate, newDate;
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT dateValue " +
                    "FROM Settings " +
                    "WHERE key = 'currentDate'"
                )
            ) {
                resultSet.next();
                oldDate = resultSet.getDate("dateValue");
            }
        }
        newDate = Date.valueOf(date);
        if (oldDate.before(newDate)) { // if skipping to future
            // insert more daily closing prices
            dateIterator.setTime(oldDate);
            while (!newDate.equals(dateIterator.getTime())) {
                dateIterator.add(Calendar.DATE, 1);
                Date currentDate = new Date(dateIterator.getTime().getTime());
                try (Statement statement = connection.createStatement()) {
                    try (
                        ResultSet resultSet = statement.executeQuery(
                            "INSERT INTO DailyClosingBalances (aid, closingDate, closingBalance) " +
                            "SELECT aid, DATE '" + currentDate.toString() + "', balance " +
                            "FROM Accounts"
                        )
                    ) {}
                }
            }
        } else if (oldDate.after(newDate)) { // if rewinding to past
            // delete daily closing prices
            dateIterator.setTime(oldDate);
            while (!newDate.equals(dateIterator.getTime())) {
                Date currentDate = new Date(dateIterator.getTime().getTime());
                try (Statement statement = connection.createStatement()) {
                    try (
                        ResultSet resultSet = statement.executeQuery(
                            "DELETE FROM DailyClosingBalances " +
                            "WHERE closingDate = DATE '" + currentDate.toString() + "'"
                        )
                    ) {}
                }
                dateIterator.add(Calendar.DATE, -1);
            }
        }
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Settings " +
                    "SET dateValue = DATE '" + date + "' " +
                    "WHERE key = 'currentDate'"
                )
            ) {}
        }
    }

    // 5 set rate
    public void setRate(String rate) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "UPDATE Settings " +
                    "SET floatValue = " + rate + " " +
                    "WHERE key = 'monthlyInterestRate'"
                )
            ) {}
        }
    }
}
