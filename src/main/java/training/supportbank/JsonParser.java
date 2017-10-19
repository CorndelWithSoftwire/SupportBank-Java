package training.supportbank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.stream.Stream;

public class JsonParser implements Parser {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Stream<Transaction> readFile(String filename) throws IOException {
        LOGGER.info("Reading CSV file " + filename);

        String fileContents = new String(Files.readAllBytes(Paths.get(filename)));

        Gson gson = buildGson();

        return Stream.of(gson.fromJson(fileContents, Transaction[].class));
    }

    private Gson buildGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (jsonElement, type, jsonDeserializationContext) ->
                LocalDate.parse(jsonElement.getAsString())
        );
        return gsonBuilder.create();
    }
}
