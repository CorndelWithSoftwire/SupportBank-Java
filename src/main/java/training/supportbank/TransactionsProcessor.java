package training.supportbank;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TransactionsProcessor {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Map<String, Account> buildAccountsFromCsv(String csvFilename) throws IOException {
        Stream<Transaction> transactions = getTransactionsFromCsv(csvFilename);
        return buildAccountsFromTransactions(transactions);
    }

    private static Stream<Transaction> getTransactionsFromCsv(String csvFileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(csvFileName));

        return lines.stream().skip(1).map(TransactionsProcessor::processLine);
    }

    private static Transaction processLine(String line) {
        String[] cells = line.split(",");
        LocalDate date = LocalDate.parse(cells[0], DATE_FORMAT);
        String from = cells[1];
        String to = cells[2];
        String narrative = cells[3];
        BigDecimal amount = new BigDecimal(cells[4]);

        return new Transaction(date, from, to, narrative, amount);
    }

    private static Map<String, Account> buildAccountsFromTransactions(Stream<Transaction> transactions) {
        Map<String, Account> accounts = new HashMap<>();

        transactions.forEach(t -> {
            addAccountIfMissing(accounts, t.getFrom());
            addAccountIfMissing(accounts, t.getTo());

            accounts.get(t.getFrom()).addOutgoingTransaction(t);
            accounts.get(t.getTo()).addIncomingTransaction(t);
        });

        return accounts;
    }

    private static void addAccountIfMissing(Map<String, Account> accounts, String owner) {
        accounts.computeIfAbsent(owner, o -> new Account(owner));
    }
}
