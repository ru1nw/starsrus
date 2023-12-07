package main.Movie;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import main.Template.UserOperation;
import oracle.jdbc.OracleConnection;

public class MovieOperation extends UserOperation {
    /*
    1. List details of movie...
    2. Top movies between...
    3. List all movies
    */

    public MovieOperation(OracleConnection connection) {
        super(connection);
    }

    // 1 list details of movie
    public void getMovie(String title) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT * " +
                    "FROM Movies "+
                    "WHERE title LIKE '" + title + "%'"
                )
            ) {
                System.out.println("Movie Title\t\t\t\t\t\t\tMovie Year\tMovie Rating");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("title")
                        + resultSet.getString("year") + "\t\t"
                        + resultSet.getString("avgrating")
                    );
                }
            }
        }
    }

    // 2 top movies between
    public void getTopMovieBetween(String start, String end) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT * " +
                    "FROM Movies " +
                    "WHERE avgrating = 10 " +
                    "AND year > " + start + " " +
                    "AND year < " + end
                )
            ) {
                System.out.println("Movie Title\t\t\t\t\t\t\tMovie Year\tMovie Rating");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("title")
                        + resultSet.getString("year") + "\t\t"
                        + resultSet.getString("avgrating")
                    );
                }
            }
        }
    }

    // 3
    public void getAllMovies() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT * " +
                    "FROM Movies"
                )
            ) {
                System.out.println("Movie Title\t\t\t\t\t\t\tMovie Year\tMovie Rating");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("title")
                        + resultSet.getString("year") + "\t\t"
                        + resultSet.getString("avgrating")
                    );
                }
            }
        }
    }
}
