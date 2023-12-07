package main.Trader;

import java.sql.SQLException;
import java.util.ArrayList;

import oracle.jdbc.OracleConnection;

import main.Movie.MovieInterface;
import main.Template.UserInterface;

public class TraderInterface extends UserInterface {
    private static String options = """
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
        String userChoice = "";
        TraderOperation operation = new TraderOperation(connection);

        while (!userChoice.equals("0")) {
            System.out.print(options);
            userChoice = myObj.nextLine();
            switch (userChoice) {
                case "1":
                    System.out.println("Deposit");
                    System.out.print("Deposit amount > ");
                    Double depositAmount = Double.valueOf(myObj.nextLine());

                    if (depositAmount <= 0) {
                        System.err.println("Error: amount must be greater than 0");
                        continue;
                    }

                    try {
                        operation.depositFunds(user, depositAmount);
                        System.out.println("$" + depositAmount + " deposited successfully!\n");
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                    break;
                case "2":
                    System.out.println("Withdrawal");
                    System.out.print("Withdraw amount > ");
                    Double withdrawAmount = Double.valueOf(myObj.nextLine());

                    if (withdrawAmount <= 0) {
                        System.err.println("Error: amount must be greater than 0");
                        continue;
                    }

                    try {
                        operation.withdrawFunds(user, withdrawAmount);
                        System.out.println("$" + withdrawAmount + " withdrawn successfully!\n");
                    } catch (SQLException e) {
                        switch (e.getErrorCode()) {
                            case 2290:
                                System.err.println("Error: cannot withdraw more than account balance");
                                break;
                            default:
                                System.err.println(e);
                        }
                    }
                    break;
                case "3":
                    System.out.println("Buy");
                    try {
                        String stocks = operation.getAllStocks();
                        System.out.println(stocks);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }

                    System.out.print("Stock symbol > ");
                    String buySymbol = myObj.nextLine();
                    System.out.print("Shares > ");
                    Double buyShares = Double.valueOf(myObj.nextLine());

                    if (buyShares <= 0) {
                        System.err.println("Error: shares must be greater than 0");
                        continue;
                    }

                    try {
                        Double totalPurchase = operation.buyStocks(user, buySymbol, buyShares);
                        System.out.println("Successfully purchased " + buyShares + " of " + buySymbol + " for a total of $" + totalPurchase);
                    } catch (SQLException e) {
                        switch (e.getErrorCode()) {
                            case 2290:
                                System.err.println("Error: cannot withdraw more than account balance");
                                break;
                            case 2291:
                            case 12899:
                                System.err.println("Error: invalid stock symbol");
                                break;
                            default:
                                System.err.println(e);
                        }
                    }
                    break;
                case "4":
                    System.out.println("Sell");

                    ArrayList<String> ownedStockSymbols;
                    try {
                        ownedStockSymbols = operation.getStockAccountSymbols(user);
                    } catch (SQLException e) {
                        System.err.println(e);
                        continue;
                    }

                    System.out.println("Owned stocks: ");
                    for (String symbol : ownedStockSymbols) {
                        System.out.println(symbol);
                    }

                    System.out.print("Stock symbol > ");
                    String sellSymbol = myObj.nextLine();

                    if (!ownedStockSymbols.contains(sellSymbol)) {
                        System.err.println("Error: symbol is not owned");
                        continue;
                    }

                    ArrayList<TraderOperation.OwnedShare> ownedShares;
                    try {
                        ownedShares = operation.getOwnedShares(user, sellSymbol);

                        System.out.println("Owned shares of " + sellSymbol + ":");
                        System.out.println("Amount\tPurchase Price");
                        for (TraderOperation.OwnedShare share : ownedShares) {
                            System.out.print(share.amount + "\t");
                            System.out.print(share.price + "\n");
                        }
                    } catch (SQLException e) {
                        switch (e.getErrorCode()) {
                            case 2291:
                                System.err.println("Error: symbol does not exist");
                                break;
                            default:
                                System.err.println(e);
                        }
                        continue;
                    }

                    System.out.print("Share price > ");
                    Double sellPrice = Double.valueOf(myObj.nextLine());

                    boolean hasPrice = false;
                    Double totalShareAmount = 0.0;
                    for (TraderOperation.OwnedShare share : ownedShares) {
                        if (share.price.equals(sellPrice)) {
                            hasPrice = true;
                            totalShareAmount = share.amount;
                            break;
                        }
                    }
                    if (!hasPrice) {
                        System.err.println("Error: purchased price does not exist");
                        continue;
                    }

                    System.out.print("Share amount > ");
                    Double sellAmount = Double.valueOf(myObj.nextLine());
                    if (sellAmount > totalShareAmount) {
                        System.err.println("Error: too many shares");
                        continue;
                    } else if (sellAmount <= 0) {
                        System.err.println("Error: shares must be greater than 0");
                        continue;
                    }

                    try {
                        Double profit = operation.sellStocks(user, sellSymbol, sellPrice, sellAmount);
                        System.out.println("Sold " + sellAmount + " shares of " + sellSymbol + " at $" + sellPrice + " for a profit of $" + profit);
                    } catch (SQLException e) {
                        switch (e.getErrorCode()) {
                            case 2290:
                                System.err.println("Error: cannot withdraw more than account balance");
                                break;
                            default:
                                System.err.println(e);
                        }
                    }

                    break;
                case "5":
                    System.out.println("Cancel");
                    try {
                        String result = operation.cancelTransaction(user);
                        if (result == null) {
                            System.err.println("Error: last transaction for this user was not a buy/sell");
                        } else {
                            System.err.println(result);
                        }
                    } catch (SQLException e) {
                        switch (e.getErrorCode()) {
                            case 2290:
                                System.err.println("Error: cannot withdraw more than account balance");
                                break;
                            default:
                                System.err.println(e);
                        }
                    }
                    break;
                case "6":
                    System.out.println("Show market account balance");

                    try {
                        Double balance = operation.getCurrentBalance(user);
                        System.out.println("Current balance: $" + balance + "\n");
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "7":
                    System.out.println("Show stock account transaction history");

                    try {
                        String history = operation.getTransactionHistory(user);
                        System.out.println(history);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "8":
                    System.out.println("List current price of a stock and the actor profile");

                    try {
                        String stocks = operation.getAllStocks();
                        System.out.println(stocks);
                    } catch (SQLException e) {
                        System.err.println(e);
                    }
                    break;
                case "9":
                    System.out.println("List movie information");
                    MovieInterface.display(connection);
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
