package training.supportbank;

import nu.xom.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class XmlParser implements Parser {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Stream<Transaction> readFile(String filename) throws FileParsingException {
        LOGGER.info("Reading XML file " + filename);

        try {
            File file = new File(filename);
            Document document = new Builder().build(file);

            List<Transaction> transactions = new ArrayList<>();

            Elements transactionElements = document.getRootElement().getChildElements();
            for (int i = 0; i < transactionElements.size(); i++) {
                Transaction transaction = parseTransaction(transactionElements.get(i));
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }

            return transactions.stream();
        } catch (ParsingException | IOException e) {
            throw new FileParsingException(e);
        }
    }

    private Transaction parseTransaction(Element transactionElement) {
        try {
            LocalDate date = parseOaDate(transactionElement.getAttributeValue("Date"));

            Element partiesElement = transactionElement.getFirstChildElement("Parties");
            if (partiesElement == null) {
                throw new XmlProcessingException("Missing element: Parties");
            }

            String from = getFirstChildContents(partiesElement, "From");
            String to = getFirstChildContents(partiesElement, "To");

            String narrative = getFirstChildContents(transactionElement, "Description");

            BigDecimal amount = getAmount(transactionElement);

            return new Transaction(date, from, to, narrative, amount);
        } catch (XmlProcessingException e) {
            reportSkippedTransaction(transactionElement, e.reason);
            return null;
        }
    }

    private static final LocalDate OA_EPOCH = LocalDate.of(1899, 12, 30);

    private LocalDate parseOaDate(String oaDate) throws XmlProcessingException {
        try {
            int daysSinceEpoch = Integer.parseInt(oaDate);
            return OA_EPOCH.plusDays(daysSinceEpoch);
        } catch (NumberFormatException e) {
            throw new XmlProcessingException("Could not parse date " + oaDate);
        }
    }

    private String getFirstChildContents(Element transactionElement, String childName) throws XmlProcessingException {
        Element childElement = transactionElement.getFirstChildElement(childName);

        if (childElement == null || childElement.getValue() == null) {
            throw new XmlProcessingException("Missing element: " + childName);
        }

        return childElement.getValue();
    }

    private BigDecimal getAmount(Element transactionElement) throws XmlProcessingException {
        Element valueElement = transactionElement.getFirstChildElement("Value");

        if (valueElement == null) {
            throw new XmlProcessingException("Missing 'Value' element");
        }

        try {
            return new BigDecimal(valueElement.getValue());
        } catch (NumberFormatException e) {
            throw new XmlProcessingException("Cannot parse value " + valueElement.getValue());
        }
    }

    private static void reportSkippedTransaction(Element transactionElement, String reason) {
        LOGGER.error(String.format("Unable to process transaction because %s: %s", reason, transactionElement.toXML()));
        System.out.println("Skipping invalid transaction: " + transactionElement.toXML());
    }

    private static class XmlProcessingException extends Exception {
        private final String reason;

        private XmlProcessingException(String reason) {
            this.reason = reason;
        }
    }
}
