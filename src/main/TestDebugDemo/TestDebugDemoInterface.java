package main.TestDebugDemo;

import oracle.jdbc.OracleConnection;

import java.sql.SQLException;

import main.Template.UserInterface;

public class TestDebugDemoInterface extends UserInterface {
    private static String options = """
        ------------------------------------------------------
        Test Debug Demo:
        1. Open Market for the Day
        2. Close Market for the Day
        3. Set a new price for a stock
        4. Set a new date to be today's date
        0. Back to Authentication
        >>>\s""";
    
    public static void display(OracleConnection connection) {
        String userChoice = "";
        TestDebugDemoOperation operation = new TestDebugDemoOperation(connection);

        while (!userChoice.equals("0")) {
            System.out.print(options);
            userChoice = myObj.nextLine();
            String stock, price, date;
            switch (userChoice) {
                case "1":
                    System.out.println("Open Market for the Day");

                    try {
                        operation.openMarket();
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "2":
                    System.out.println("Close Market for the Day");

                    try {
                        operation.closeMarket();
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "3":
                    System.out.println("Set a new price for a stock");
                    System.out.print("stock stymbol > ");
                    stock = myObj.nextLine();
                    System.out.print("new price > ");
                    price = myObj.nextLine();

                    try {
                        operation.setStockPrice(stock, price);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "4":
                    System.out.println("Set a new date to be today's date");
                    System.out.print("new date (format: yyyy-mm-dd) > ");
                    date = myObj.nextLine();

                    try {
                        operation.setDate(date);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "0":
                    System.out.println("Back to Authentication");
                    break;
                default:
                    System.out.println("Please enter a number between 0 and 1 (no leading zeros)");
            }
        }
    }
}
