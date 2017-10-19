package training.supportbank;

import java.io.IOException;
import java.util.stream.Stream;

public interface Parser {

    Stream<Transaction> readFile(String filename) throws IOException;

    static Parser forFilename(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        switch (extension) {
            case "json":
                return new JsonParser();
            case "csv":
                return new CsvParser();
            default:
                return null;
        }
    }
}
