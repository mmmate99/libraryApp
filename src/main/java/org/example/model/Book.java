package org.example.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String title;
    private String author;
    private String genre;
    private boolean isBorrowed;
    private String borrowedBy;
    private String borrowedDate;
    private String returnedDate;
    private String isbn;
    private String dueDate;

    public Book(){}

    public Book(String title, String author, String genre, String isbn){
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isbn = isbn;
        this.isBorrowed = false;
        this.borrowedBy= null;
        this.borrowedDate= null;
        this.returnedDate= null;
        this.dueDate=null;
    }

    public String getTitle(){return title;}
    public void setTitle(String title){this.title = title;}

    public String getAuthor(){return author;}
    public void setAuthor(String author){this.author = author;};

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public boolean isBorrowed(){return isBorrowed;}
    public void setBorrowed(boolean borrowed){this.isBorrowed = borrowed;}

    public String getBorrowedBy() { return borrowedBy; }
    public void setBorrowedBy(String borrowedBy) { this.borrowedBy = borrowedBy; }

    public String getBorrowedDate() { return borrowedDate; }
    public void setBorrowedDate(String borrowedDate) { this.borrowedDate = borrowedDate; }

    public String getReturnedDate() { return returnedDate; }
    public void setReturnedDate(String returnedDate) { this.returnedDate = returnedDate; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public long getDaysLate() {
        try {
            if (dueDate != null && returnedDate != null) {
                LocalDate due = LocalDate.parse(dueDate);
                LocalDate returned = LocalDate.parse(returnedDate);
                return Math.max(ChronoUnit.DAYS.between(due, returned), 0);
            }
        } catch (DateTimeParseException e) {
            System.err.println("Hibás dátumformátum: " + e.getMessage());
        }
        return 0;
    }


    @Override
    public String toString() {
        if (isBorrowed) {
            return String.format("%s - %s - %s [Kölcsönzött: %s (%s) - %s]", title, author, genre, borrowedBy, borrowedDate, dueDate);
        } else if (returnedDate != null) {
            return String.format("%s - %s - %s [Elérhető, visszahozva: %s]", title, author, genre, returnedDate);
        } else {
            return String.format("%s - %s - %s [Elérhető]", title, author, genre);
        }
    }
}
