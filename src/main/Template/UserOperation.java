package main.Template;

import oracle.jdbc.OracleConnection;

public abstract class UserOperation {
    protected OracleConnection connection;

    public UserOperation(OracleConnection connection) {
        this.connection = connection;
    }
}
