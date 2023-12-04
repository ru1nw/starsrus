package main.Trader;

import main.Template.UserOperation;
import oracle.jdbc.OracleConnection;

public class TraderOperation extends UserOperation {
    /*
    1. Deposit
    2. Withdrawal
    3. Buy
    4. Sell
    5. Cancel
    6. Show market account balance
    7. Show stock account transaction history
    8. List current price of a stock and the actor profile
    9. List movie information
    */

    public TraderOperation(OracleConnection connection) {
        super(connection);
    }
}
