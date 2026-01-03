package com.spritehealth.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Provides a properly configured Gson instance that can handle LocalDate and other Java 8+ types.
 */
public class GsonProvider {
    // Singleton instance of Gson configured for LocalDate and pretty printing
    private static final Gson GSON_INSTANCE;

    // Static block to initialize the Gson instance with custom settings
    static {
        GSON_INSTANCE = new GsonBuilder()
                // Register custom adapter for LocalDate serialization/deserialization
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                // Enable pretty printing for JSON output
                .setPrettyPrinting()
                .create();
    }

    /**
     * Returns the singleton Gson instance configured for the application.
     * @return configured Gson instance
     */
    public static Gson getGson() {
        return GSON_INSTANCE;
    }

    /**
     * Custom TypeAdapter for LocalDate to handle JSON serialization/deserialization.
     * Converts LocalDate to ISO-8601 string and vice versa.
     */
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        // Formatter for ISO-8601 date format (yyyy-MM-dd)
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

        /**
         * Serializes LocalDate to JSON as a string.
         * @param out JsonWriter to write to
         * @param value LocalDate value to serialize
         * @throws IOException if writing fails
         */
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(FORMATTER));
            }
        }

        /**
         * Deserializes a JSON string to LocalDate.
         * @param in JsonReader to read from
         * @return LocalDate parsed from JSON string
         * @throws IOException if reading fails
         */
        @Override
        public LocalDate read(JsonReader in) throws IOException {
            String dateString = in.nextString();
            return LocalDate.parse(dateString, FORMATTER);
        }
    }
}
