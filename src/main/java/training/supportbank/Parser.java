package training.supportbank;

import java.util.stream.Stream;

public interface Parser {

    Stream<Transaction> readFile(String filename) throws FileParsingException;

    static Parser forFilename(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        switch (extension) {
            case "json":
                return new JsonParser();
            case "csv":
                return new CsvParser();
            case "xml":
                return new XmlParser();
            default:
                return null;
        }
    }
}
