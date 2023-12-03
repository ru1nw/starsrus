package main.Util;
/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/
/*
   DESCRIPTION    
   This code sample shows how to use JDBC and the OracleDataSource API to establish a
   connection to your database.
   This is adapted from an official Oracle sample project
   (https://github.com/oracle-samples/oracle-db-examples/blob/main/java/jdbc/ConnectionSamples/DataSourceSample.java)
   to suit the needs of your CS174A project.
    
    Step 1: Download the Zipped JDBC driver (ojdbc11.jar) and Companion Jars from this
            link:
            https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
            Extract the zipped contents into the lib folder. This allows your code to
            interface properly with JDBC.
    Step 2: Enter the database details (DB_USER, DB_PASSWORD and DB_URL) in this file.
            Note that DB_URL will require you to know the path to your connection
            wallet.
    Step 3: Run the file with "java -cp lib/ojdbc11.jar ./src/TestConnection.java"
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.sql.DatabaseMetaData;

public class TestConnection {
    // The recommended format of a connection URL is:
    // "jdbc:oracle:thin:@<DATABASE_NAME_LOWERCASE>_tp?TNS_ADMIN=<PATH_TO_WALLET>"
    // where
    // <DATABASE_NAME_LOWERCASE> is your database name in lowercase
    // and
    // <PATH_TO_WALLET> is the path to the connection wallet on your machine.
    final static String DB_URL = "jdbc:oracle:thin:@cs174adb_tp?TNS_ADMIN=" + AppProperties.PATH_TO_WALLET;
    final static String DB_USER = AppProperties.DB_USER;
    final static String DB_PASSWORD = AppProperties.DB_PASSWORD;

    // This method creates a database connection using
    // oracle.jdbc.pool.OracleDataSource.
    public static void main(String args[]) throws SQLException {
        Properties info = new Properties();

        System.out.println("Initializing connection properties...");
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        System.out.println("Creating OracleDataSource...");
        OracleDataSource ods = new OracleDataSource();

        System.out.println("Setting connection properties...");
        ods.setURL(DB_URL);
        ods.setConnectionProperties(info);

        // With AutoCloseable, the connection is closed automatically
        try (OracleConnection connection = (OracleConnection) ods.getConnection()) {
            System.out.println("Connection established!");
            // Get JDBC driver name and version
            DatabaseMetaData dbmd = connection.getMetaData();
            System.out.println("Driver Name: " + dbmd.getDriverName());
            System.out.println("Driver Version: " + dbmd.getDriverVersion());
            // Print some connection properties
            System.out.println(
                "Default Row Prefetch Value: " + connection.getDefaultRowPrefetch()
            );
            System.out.println("Database username: " + connection.getUserName());
            System.out.println();
            // Perform some database operations
            insertTA(connection);
            printInstructors(connection);
        } catch (Exception e) {
            System.out.println("CONNECTION ERROR:");
            System.out.println(e);
        }
    }

    // Inserts another TA into the Instructors table.
    public static void insertTA(Connection connection) throws SQLException {
        System.out.println("Preparing to insert TA into Instructors table...");
        // Statement and ResultSet are AutoCloseable and closed automatically. 
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT INTO INSTRUCTORS VALUES (3, 'Momin Haider', 'TA')"
                )
            ) {}
        } catch (Exception e) {
            System.out.println("ERROR: insertion failed.");
            System.out.println(e);
        }
    }

    // Displays data from Instructors table.
    public static void printInstructors(Connection connection) throws SQLException {
        // Statement and ResultSet are AutoCloseable and closed automatically. 
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM INSTRUCTORS"
                )
            ) {
                System.out.println("INSTRUCTORS:");
                System.out.println("I_ID\tI_NAME\t\tI_ROLE");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("I_ID") + "\t"
                        + resultSet.getString("I_NAME") + "\t"
                        + resultSet.getString("I_ROLE")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: selection failed.");
            System.out.println(e);
        }
    }
}
