package training.supportbank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Console;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionsProcessor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Map<String, Account> buildAccountsFromCsv(String csvFilename) throws IOException {
        Stream<Transaction> transactions = getTransactionsFromCsv(csvFilename);
        return buildAccountsFromTransactions(transactions);
    }

    private static Stream<Transaction> getTransactionsFromCsv(String csvFileName) throws IOException {
        LOGGER.info("Reading CSV file " + csvFileName);
        List<String> lines = Files.readAllLines(Paths.get(csvFileName));

        return lines.stream().skip(1).map(TransactionsProcessor::processLine).filter(Optional::isPresent).map(Optional::get);
    }

    private static Optional<Transaction> processLine(String line) {
        LOGGER.debug("Parsing line: " + line);
        String[] fields = line.split(",");

        if (fields.length != 5) {
            reportSkippedTransaction(line, "Wrong number of fields");
            return Optional.empty();
        }

        try {
            LocalDate date = LocalDate.parse(fields[0], DATE_FORMAT);
            String from = fields[1];
            String to = fields[2];
            String narrative = fields[3];
            BigDecimal amount = new BigDecimal(fields[4]);

            return Optional.of(new Transaction(date, from, to, narrative, amount));
        } catch (DateTimeParseException e) {
            reportSkippedTransaction(line, "Invalid date");
        } catch (NumberFormatException e) {
            reportSkippedTransaction(line, "Invalid amount");
        }

        return Optional.empty();
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
        accounts.computeIfAbsent(owner, o -> {
            LOGGER.info("Adding account for " + owner);
            return new Account(owner);
        });
    }

    private static void reportSkippedTransaction(String line, String reason) {
        LOGGER.error(String.format("Unable to process transaction because %s: %s", reason, line));
        System.out.println("Skipping invalid transaction: " + line);
    }
}
