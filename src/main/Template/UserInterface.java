package main.Template;
import java.util.Scanner;

public abstract class UserInterface {
    private static String options;
    
    public static void show() {
        Scanner myObj = new Scanner(System.in);
        System.out.println("User interface template");
        String userChoice = myObj.nextLine();
        switch (userChoice) {
            case "1":
                System.out.println("1st choice");
                break;
            case "2":
                System.out.println("2nd choice");
                break;
            default:
                System.out.println("Please enter a number");
        }
        myObj.close();
    }
}
