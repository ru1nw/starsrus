package main.Template;

import oracle.jdbc.OracleConnection;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public abstract class UserOperation {
    protected OracleConnection connection;

    public UserOperation(OracleConnection connection) {
        this.connection = connection;
    }

    public final Integer getNextID(String tableName, String idField, Statement statement) throws SQLException {
        try (
            ResultSet resultSet = statement.executeQuery(
                "SELECT MAX(T." + idField + ") as maxID " +
                "FROM " + tableName + " T"
            )
        ) {
            resultSet.next();
            return resultSet.getInt("maxID")+1;
        }
    }

    public final Date getCurrentDate(Statement statement) throws SQLException {
        try (
            ResultSet resultSet = statement.executeQuery(
                "SELECT dateValue " +
                "FROM SETTINGS " +
                "WHERE key = 'currentDate'"
            )
        ) {
            resultSet.next();
            return resultSet.getDate("dateValue");
        }
    }

    public final Boolean getIsMarketOpen(Statement statement) throws SQLException {
        try (
            ResultSet resultSet = statement.executeQuery(
                "SELECT boolValue " +
                "FROM SETTINGS " +
                "WHERE key = 'isMarketOpen'"
            )
        ) {
            resultSet.next();
            return resultSet.getBoolean("boolValue");
        }
    }

    public ArrayList<Integer> getStockAccountsAid(String username, Statement statement) throws SQLException {
        ArrayList<Integer> out = new ArrayList<Integer>();
        try (
            ResultSet resultSet = statement.executeQuery(
                "SELECT aid " +
                "FROM StockAccounts NATURAL JOIN Accounts " +
                "WHERE uname = '" + username + "'"
            )
        ) {
            while (resultSet.next()) {
                int resultAid = resultSet.getInt("aid");
                out.add(resultAid);
            }

            return out;
        }
    }
}
