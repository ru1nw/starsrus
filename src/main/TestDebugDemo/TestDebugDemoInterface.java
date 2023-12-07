package main.TestDebugDemo;

import oracle.jdbc.OracleConnection;

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
            switch (userChoice) {
                case "1":
                    System.out.println("Open Market for the Day");
                    break;
                case "2":
                    System.out.println("Close Market for the Day");
                    break;
                case "3":
                    System.out.println("Set a new price for a stock");
                    break;
                case "4":
                    System.out.println("Set a new date to be today's date");
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
