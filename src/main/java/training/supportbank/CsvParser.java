package training.supportbank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CsvParser implements Parser {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Stream<Transaction> readFile(String filename) throws IOException {
        LOGGER.info("Reading CSV file " + filename);
        List<String> lines = Files.readAllLines(Paths.get(filename));

        return lines.stream().skip(1).map(CsvParser::processLine).filter(Optional::isPresent).map(Optional::get);
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

    private static void reportSkippedTransaction(String line, String reason) {
        LOGGER.error(String.format("Unable to process transaction because %s: %s", reason, line));
        System.out.println("Skipping invalid transaction: " + line);
    }
}
