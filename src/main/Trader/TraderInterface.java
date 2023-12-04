package main.Trader;
import java.util.Scanner;

import oracle.jdbc.OracleConnection;

import main.Template.UserInterface;

public class TraderInterface extends UserInterface {
    private static String options = """
        \n\n\n\n\n
        ------------------------------------------------------
        Trader options:
        1. Deposit
        2. Withdrawal
        3. Buy
        4. Sell
        5. Cancel
        6. Show market account balance
        7. Show stock account transaction history
        8. List current price of a stock and the actor profile
        9. List movie information
        0. Logout
        >>>\s""";
    
    public static void display(OracleConnection connection, String user) {
        Scanner myObj = new Scanner(System.in);
        String userChoice = "";
        while (!userChoice.equals("0")) {
            System.out.print(options);
            userChoice = myObj.nextLine();
            switch (userChoice) {
                case "1":
                    System.out.println("Deposit");
                    break;
                case "2":
                    System.out.println("Withdrawal");
                    break;
                case "3":
                    System.out.println("Buy");
                    break;
                case "4":
                    System.out.println("Sell");
                    break;
                case "5":
                    System.out.println("Cancel");
                    break;
                case "6":
                    System.out.println("Show market account balance");
                    break;
                case "7":
                    System.out.println("Show stock account transaction history");
                    break;
                case "8":
                    System.out.println("List current price of a stock and the actor profile");
                    break;
                case "9":
                    System.out.println("List movie information");
                    break;
                case "0":
                    System.out.println("Logout");
                    break;
                default:
                    System.out.println("Please enter a number between 0 and 9 (no leading zeros)");
            }
        }
        myObj.close();
    }
}
