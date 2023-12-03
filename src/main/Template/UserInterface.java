package main.Template;

import java.util.Scanner;

public abstract class UserInterface {
    private static String options;
    
    public static void display() {
        Scanner myObj = new Scanner(System.in);
        System.out.println("User interface template");
        String userChoice = myObj.nextLine();
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
        myObj.close();
    }
}
