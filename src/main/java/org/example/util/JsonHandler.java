package org.example.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Book;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonHandler {
    public static final ObjectMapper mapper = new ObjectMapper();

    public static List<Book> load(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) return new java.util.ArrayList<>();
            return mapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Cannot load books", e);
        }
    }

    public static void save(String path, List<Book> books){
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), books);
        }catch (IOException e){
            throw new RuntimeException("Cannot save books", e);
        }
    }
}
