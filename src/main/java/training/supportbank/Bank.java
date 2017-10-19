package training.supportbank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Bank {
    private static final Logger LOGGER = LogManager.getLogger();

    private Map<String, Account> accounts = new HashMap<>();

    public void processAccounts(Stream<Transaction> transactions) {
        transactions.forEach(t -> {
            addAccountIfMissing(t.getFromAccount());
            addAccountIfMissing(t.getToAccount());

            accounts.get(t.getFromAccount()).addOutgoingTransaction(t);
            accounts.get(t.getToAccount()).addIncomingTransaction(t);
        });
    }

    private void addAccountIfMissing(String owner) {
        accounts.computeIfAbsent(owner, o -> {
            LOGGER.info("Adding account for " + owner);
            return new Account(owner);
        });
    }

    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    public Account getAccount(String owner) {
        return accounts.get(owner);
    }
}
