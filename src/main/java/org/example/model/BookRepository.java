package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.util.JsonHandler;

public class BookRepository {
    private List<Book> books= new ArrayList<>();

    public void loadBooks(String path){
        books= JsonHandler.load(path);
    }

    public void saveBooks(String path){
        JsonHandler.save(path, books);
    }

    public boolean existsByIsbn(String isbn) {
        return books.stream().anyMatch(b -> b.getIsbn().equalsIgnoreCase(isbn));
    }

    public boolean addBook(Book book){
        if (existsByIsbn(book.getIsbn())) return false;
        books.add(book);
        return true;
    }

    public void removeBook(Book book){books.remove(book);}

    public List<Book> getBooks(){ return books;}

    public List<Book> search(String title, String author, String genre, String isbn){
        return books.stream().filter(b ->
                (title == null || b.getTitle().toLowerCase().contains(title.toLowerCase())) &&
                        (author == null || b.getAuthor().toLowerCase().contains(author.toLowerCase())) &&
                        (genre == null || b.getGenre().equalsIgnoreCase(genre)) &&
                        (isbn == null || b.getIsbn().equalsIgnoreCase(isbn))
        ).collect(Collectors.toList());
    }
}
