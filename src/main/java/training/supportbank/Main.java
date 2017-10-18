package training.supportbank;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^List (.*)$");

    public static void main(String args[]) throws IOException {
        Map<String, Account> accounts = TransactionsProcessor.buildAccountsFromCsv("Transactions2014.csv");
        printBanner();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            parseAndExecuteCommand(command, accounts);
        }
    }

    private static void printBanner() {
        System.out.println("Welcome to SupportBank!");
        System.out.println("=======================");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  List All - list all account balances");
        System.out.println("  List [Account] - list transactions for the specified account");
        System.out.println();
    }

    private static void parseAndExecuteCommand(String command, Map<String, Account> accounts) {
        Matcher matcher = COMMAND_PATTERN.matcher(command);

        if (matcher.find()) {
            String accountName = matcher.group(1);
            if (accountName.equals("All")) {
                listAllAccounts(accounts.values());
            } else {
                listSingleAccount(accounts.get(accountName));
            }
        }
    }

    private static void listAllAccounts(Collection<Account> accounts) {
        System.out.println("All accounts");

        for (Account account : accounts) {
            BigDecimal balance = account.calculateBalance();
            String owingMessage = balance.compareTo(BigDecimal.ZERO) < 0 ? "owes" : "is owed";
            String balanceString = CURRENCY_FORMAT.format(balance.abs());
            System.out.println("  " + account.getOwner() + " " + owingMessage + " " + balanceString);
        }

        System.out.println();
    }

    private static void listSingleAccount(Account account) {
        System.out.println("Account " + account.getOwner());

        for (Transaction transaction : account.getAllTransactionsInDateOrder()) {
            System.out.println(String.format("  %s: %s paid %s %s for %s",
                    transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    transaction.getFrom(),
                    transaction.getTo(),
                    CURRENCY_FORMAT.format(transaction.getAmount()),
                    transaction.getNarrative()
            ));
        }

        System.out.println();
    }
}
