package training.supportbank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class AccountLister {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final Logger LOGGER = LogManager.getLogger();

    public static void listAccounts(Collection<Account> accounts) {
        LOGGER.debug("Listing all accounts");
        System.out.println("All accounts");

        for (Account account : accounts) {
            BigDecimal balance = account.calculateBalance();
            String owingMessage = balance.compareTo(BigDecimal.ZERO) < 0 ? "owes" : "is owed";
            String balanceString = CURRENCY_FORMAT.format(balance.abs());
            System.out.println("  " + account.getOwner() + " " + owingMessage + " " + balanceString);
        }

        System.out.println();
    }

    public static void listSingleAccount(Account account) {
        LOGGER.debug("Listing account for " + account.getOwner());
        System.out.println("Account " + account.getOwner());

        for (Transaction transaction : account.getAllTransactionsInDateOrder()) {
            System.out.println(String.format("  %s: %s paid %s %s for %s",
                    transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    transaction.getFromAccount(),
                    transaction.getToAccount(),
                    CURRENCY_FORMAT.format(transaction.getAmount()),
                    transaction.getNarrative()
            ));
        }

        System.out.println();
    }
}
