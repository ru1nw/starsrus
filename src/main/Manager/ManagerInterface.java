package main.Manager;

import java.sql.SQLException;

import oracle.jdbc.OracleConnection;

import main.Movie.MovieInterface;
import main.Template.UserInterface;

public class ManagerInterface extends UserInterface {
    private static String options = """
        ------------------------------------------------------
        Manager options:
        1. Add Interest
        2. Generate Monthly Statement
        3. List Active Customers
        4. Generate Government Drug & Tax Evasion Report (DTER)
        5. Customer Report
        6. Delete Transactions
        0. Logout
        >>>\s""";
    
    public static void display(OracleConnection connection, String user) {
        String userChoice = "";
        ManagerOperation operation = new ManagerOperation(connection);

        while (!userChoice.equals("0")) {
            System.out.print(options);
            userChoice = myObj.nextLine();
            String username;
            switch (userChoice) {
                case "1":
                    System.out.println("Add Interest");

                    try {
                        String result = operation.addInterest();
                        System.out.println(result);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    break;
                case "2":
                    System.out.println("Generate Monthly Statement");
                    System.out.print("username > ");
                    username = myObj.nextLine();

                    try {
                        String result = operation.getStatement(username);
                        System.out.println(result);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    break;
                case "3":
                    System.out.println("List Active Customers");

                    try {
                        String result = operation.getActiveCustomer();
                        System.out.println(result);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    break;
                case "4":
                    System.out.println("Generate Government Drug & Tax Evasion Report (DTER)");

                    try {
                        String result = operation.getDTER();
                        System.out.println(result);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    break;
                case "5":
                    System.out.println("Customer Report");
                    System.out.print("username > ");
                    username = myObj.nextLine();

                    try {
                        String result = operation.getCustomerReport(username);
                        System.out.println(result);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    break;
                case "6":
                    System.out.println("Delete Transactions");

                    try {
                        String result = operation.deleteTransactions();
                        System.out.println(result);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    break;
                case "0":
                    System.out.println("Logout");
                    break;
                default:
                    System.out.println("Please enter a number between 0 and 9 (no leading zeros)");
            }
        }
    }
}
