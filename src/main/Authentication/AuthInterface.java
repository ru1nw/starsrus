package main.Authentication;

import java.util.Scanner;

import oracle.jdbc.OracleConnection;
import main.Manager.ManagerInterface;
import main.Template.UserInterface;
import main.TestDebugDemo.TestDebugDemoInterface;
import main.Trader.TraderInterface;

public class AuthInterface extends UserInterface {
    private static String options = """
        \n\n\n\n\n
        ------------------------------------------------------
        Get started:
        1. Customer sign in
        2. Customer register
        3. Manager sign in
        4. Test Debug Demo Operations
        0. Exit
        >>>\s""";
    
    public static void display(OracleConnection connection) {
        String userChoice = "";
        AuthOperation operation = new AuthOperation(connection);

        while (!userChoice.equals("0")) {
            System.out.print(options);
            userChoice = myObj.nextLine();
            String username, password, user;
            switch (userChoice) {
                case "1":
                    System.out.println("Customer sign in");
                    System.out.print("username > ");
                    username = myObj.nextLine();
                    System.out.print("password > ");
                    password = myObj.nextLine();
                    user = operation.signInCustomer(username, password);
                    if (user.isEmpty()) {
                        System.err.println("ERROR: login failed.");
                    } else {
                        TraderInterface.display(connection, user);
                        userChoice = "0";
                    }
                    break;
                case "2":
                    System.out.println("Customer register");
                    System.out.print("name > ");
                    String name = myObj.nextLine();
                    System.out.print("username > ");
                    username = myObj.nextLine();
                    System.out.print("password > ");
                    password = myObj.nextLine();
                    System.out.print("retype password > ");
                    String repassword = myObj.nextLine();
                    if (!password.equals(repassword)) {
                        System.err.println("password does not match, please try again.");
                        break;
                    }
                    System.out.print("state > ");
                    String state = myObj.nextLine();
                    System.out.print("phone > ");
                    String phone = myObj.nextLine();
                    System.out.print("email > ");
                    String email = myObj.nextLine();
                    System.out.print("taxid > ");
                    String taxid = myObj.nextLine();
                    user = operation.signUpCustomer(name, username, password, state, phone, email, taxid);
                    if (user.isEmpty()) {
                        System.err.println("ERROR: login failed.");
                    } else {
                        TraderInterface.display(connection, user);
                        userChoice = "0";
                    }
                    break;
                case "3":
                    System.out.println("Manager sign in");
                    System.out.print("username > ");
                    username = myObj.nextLine();
                    System.out.print("password > ");
                    password = myObj.nextLine();
                    user = operation.signInManager(username, password);
                    if (user.isEmpty()) {
                        System.err.println("ERROR: login failed.");
                    } else {
                        // uncomment the line below when ManagerInterface is implemented
                        ManagerInterface.display(connection, user);
                        userChoice = "0";
                    }
                    break;
                case "4":
                    TestDebugDemoInterface.display(connection);
                    break;
                case "0":
                    System.out.println("Exit");
                    break;
                default:
                    System.out.println("Please enter a number between 0 and 3 (no leading zeros)");
            }
        }
        myObj.close();
    }
}
