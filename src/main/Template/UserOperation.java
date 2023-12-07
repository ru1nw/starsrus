package main.Template;

import oracle.jdbc.OracleConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class UserOperation {
    protected OracleConnection connection;

    public UserOperation(OracleConnection connection) {
        this.connection = connection;
    }

    public final Integer getNextID(String tableName, String idField) throws SQLException {
        try (Statement statement = connection.createStatement()) {
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
    }
}
