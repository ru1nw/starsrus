package main.Movie;

import java.sql.SQLException;
import java.util.Scanner;

import oracle.jdbc.OracleConnection;
import main.Template.UserInterface;

public class MovieInterface extends UserInterface {
    private static String options = """
        ------------------------------------------------------
        Movie options:
        1. List details of movie...
        2. Top movies between...
        3. List all movies
        0. Back to Trader Options
        >>>\s""";
    
    public static void display(OracleConnection connection) {
        String userChoice = "";
        MovieOperation operation = new MovieOperation(connection);

        while (!userChoice.equals("0")) {
            System.out.print(options);
            userChoice = myObj.nextLine();
            String title, year, startYear, endYear;
            switch (userChoice) {
                case "1":
                    System.out.println("List details of movie");
                    System.out.print("name > ");
                    title = myObj.nextLine();

                    try {
                        operation.getMovie(title);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "2":
                    System.out.println("Top movies between");
                    System.out.print("start year > ");
                    startYear = myObj.nextLine();
                    System.out.print("end year > ");
                    endYear = myObj.nextLine();
                    try {
                        operation.getTopMovieBetween(startYear, endYear);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "3":
                    System.out.println("List all movies");
                    try {
                        operation.getAllMovies();
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "0":
                    System.out.println("Back to Trader Options");
                    break;
                default:
                    System.out.println("Please enter a number between 0 and 3 (no leading zeros)");
            }
        }
    }
}
