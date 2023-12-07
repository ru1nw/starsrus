package main.Template;

import java.util.Scanner;

import oracle.jdbc.OracleConnection;

public abstract class UserInterface {
    private static String options = "User interface template";
    protected static Scanner myObj = new Scanner(System.in);;
    
    public static void display(OracleConnection connection) {
        String userChoice = "";

        while (!userChoice.equals("0")) {
            System.out.print(options);
            userChoice = myObj.nextLine();
            switch (userChoice) {
                case "1":
                    System.out.println("1st choice");
                    break;
                case "0":
                    System.out.println("cancel/exit");
                    break;
                default:
                    System.out.println("Please enter a number between 0 and 1 (no leading zeros)");
            }
        }
        myObj.close();
    }
}
