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
 * Provides a properly configured Gson instance that can handle LocalDate and other Java 8+ types
 */
public class GsonProvider {
    private static final Gson GSON_INSTANCE;
    
    static {
        GSON_INSTANCE = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }
    
    public static Gson getGson() {
        return GSON_INSTANCE;
    }
    
    /**
     * Custom TypeAdapter for LocalDate to handle JSON serialization/deserialization
     */
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
        
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(FORMATTER));
            }
        }
        
        @Override
        public LocalDate read(JsonReader in) throws IOException {
            String dateString = in.nextString();
            return LocalDate.parse(dateString, FORMATTER);
        }
    }
}
