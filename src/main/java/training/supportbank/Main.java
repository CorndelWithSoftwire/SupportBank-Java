package training.supportbank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    private static Bank bank = new Bank();

    public static void main(String args[]) throws IOException {
        LOGGER.info("SupportBank starting up!");

        printBanner();
        showPrompt();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            parseAndExecuteCommand(command);
            showPrompt();
        }
        LOGGER.info("SupportBank closed!");
    }

    private static void printBanner() {
        System.out.println("Welcome to SupportBank!");
        System.out.println("=======================");
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  List All - list all account balances");
        System.out.println("  List [Account] - list transactions for the specified account");
        System.out.println("  Import File [Filename] - import transactions from the specified file");
        System.out.println();
    }

    private static void showPrompt() {
        System.out.print("Enter command > ");
    }

    private static void parseAndExecuteCommand(String command) {
        if (command.startsWith("Import File ")) {
            String filename = command.substring("Import File ".length());
            importFile(filename);
        } else if (command.startsWith("List ")) {
            String accountName = command.substring("List ".length());
            if (accountName.equals("All")) {
                AccountLister.listAccounts(bank.getAllAccounts());
            } else {
                AccountLister.listSingleAccount(bank.getAccount(accountName));
            }
        } else {
            LOGGER.warn("Did not recognise command: " + command);
            System.out.println("Did not understand the command: " + command);
        }
    }

    private static void importFile(String filename) {
        LOGGER.info("Importing file " +  filename);
        Parser parser = Parser.forFilename(filename);

        if (parser == null) {
            LOGGER.error("No parser found for filename " + filename);
            System.out.println("Do not know how to parse file '" + filename + "', skipping it!");
            return;
        }

        try{
            Stream<Transaction> transactions = parser.readFile(filename);
            bank.processAccounts(transactions);
            LOGGER.info("Import of file '" + filename + "' succeeded");
            System.out.println("Import successful!");

        } catch (FileParsingException e) {
            LOGGER.error(String.format("Error opening file '%s'", filename), e);
            System.out.println("Error opening file - import failed");
        }
    }
}
