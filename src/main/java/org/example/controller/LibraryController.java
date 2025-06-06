package org.example.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.model.Book;
import org.example.model.BookRepository;

import java.util.*;
import java.util.stream.Collectors;

public class LibraryController {
    @FXML
    public TextField titleField;
    @FXML
    public TextField authorField;
    @FXML
    public TextField isbnField;
    @FXML
    public TextField searchTitleField;
    @FXML
    public TextField searchAuthorField;
    @FXML private TextField searchIsbnField;
    @FXML
    public Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button resetButton;
    @FXML private Button exportButton;
    @FXML
    public ListView<Book> bookList;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML
    public ComboBox<String> genreFilterComboBox;
    @FXML
    public ComboBox<String> genreComboBox;
    private boolean isModified = false;

    private boolean suppressComboBoxEvents = false;

    public BookRepository repo= new BookRepository();
    private final String filePath="books.json";

    public void initialize(){
        genreComboBox.setItems(FXCollections.observableArrayList(
                 "Regény", "Tudományos", "Fantasy", "Krimi", "Életrajz", "Ismeretterjesztő"
        ));
        genreComboBox.setValue("Regény");

        titleField.textProperty().addListener((obs, oldVal, newVal) -> isModified = true);
        authorField.textProperty().addListener((obs, oldVal, newVal) -> isModified = true);
        isbnField.textProperty().addListener((obs, oldVal, newVal) -> isModified = true);
        genreComboBox.valueProperty().addListener((obs, oldVal, newVal) -> isModified = true);

        repo.loadBooks(filePath);
        refreshList();
    }

    @FXML
    public void onAdd(){
        if(!checkUnsavedChanges()) return;

        String title = titleField.getText();
        String author = authorField.getText();
        String isbn = isbnField.getText();
        String genre = genreComboBox.getValue();
        if(title.isEmpty() || author.isEmpty() || genre.isEmpty() || isbn.isEmpty()){
            showAlert("Kitöltési hiba", "Minden mezőt ki kell tölteni!");
            return;
        }
        if (repo.existsByIsbn(isbn)) {
            showAlert("Duplikáció", "Már létezik könyv ezzel az ISBN-nel!");
            return;
        }
        repo.addBook(new Book(title, author, genre, isbn));
        clearFields();
        refreshList();
    }

    @FXML
    public void onFilter(){
        String selected = filterComboBox.getValue();
        List<Book> filtered;

        if("Csak kölcsönzött".equals(selected)){
            filtered = repo.getBooks().stream().filter(Book::isBorrowed).toList();
        } else if ("Csak elérhető".equals(selected)) {
            filtered = repo.getBooks().stream().filter(b -> !b.isBorrowed()).toList();
        }else{
            filtered = repo.getBooks();
        }

        bookList.setItems(FXCollections.observableList(filtered));
    }

    @FXML
    public void onSort(){
        String selected = sortComboBox.getValue();
        List<Book> sorted = new ArrayList<>(repo.getBooks());

        switch (selected){
            case "Cím szerint (A-Z)" -> sorted.sort(Comparator.comparing(Book::getTitle));
            case "Cím szerint (Z-A)" -> sorted.sort(Comparator.comparing(Book::getTitle).reversed());
            case "Szerző szerint (A-Z)"-> sorted.sort(Comparator.comparing(Book::getAuthor));
            case "Szerző szerint (Z-A)"-> sorted.sort(Comparator.comparing(Book::getAuthor).reversed());
        }

        bookList.setItems(FXCollections.observableList(sorted));
    }

    @FXML
    public void onResetSearch(){
        searchTitleField.clear();
        searchAuthorField.clear();
        if(searchIsbnField != null) searchIsbnField.clear();
        if(genreFilterComboBox != null) genreFilterComboBox.getSelectionModel().clearSelection();

        resetButton.setVisible(false);
        bookList.setItems(FXCollections.observableList(repo.getBooks()));

        if(filterComboBox != null) filterComboBox.getSelectionModel().clearSelection();
        if(sortComboBox != null) sortComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    public void onDelete(){
        Book selected = bookList.getSelectionModel().getSelectedItem();
        if(selected != null){
            repo.removeBook(selected);
            refreshList();
        }
    }

    @FXML
    public void onEdit() {
        Book selected = bookList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Hiba", "Nincs kiválasztva könyv.");
            return;
        }

        titleField.setText(selected.getTitle());
        authorField.setText(selected.getAuthor());
        isbnField.setText(selected.getIsbn());

        // "mentés" gomb most nem újként hoz létre, hanem szerkeszt
        addButton.setText("Mentés");
        addButton.setOnAction(e -> onSaveEdit(selected));

        genreComboBox.setValue(selected.getGenre());
    }

    public void onSaveEdit(Book selected) {
        String title = titleField.getText();
        String author = authorField.getText();
        String isbn = isbnField.getText();
        String genre = genreComboBox.getValue();

        if (!selected.getIsbn().equals(isbn) && repo.existsByIsbn(isbn)) {
            showAlert("Duplikált ISBN", "Már létezik ilyen ISBN.");
            return;
        }

        selected.setTitle(title);
        selected.setAuthor(author);
        selected.setIsbn(isbn);
        selected.setGenre(genre);

        refreshList();
        clearFields();
        addButton.setText("Hozzáadás");
        addButton.setOnAction(e -> onAdd());

        genreComboBox.setValue(selected.getGenre());

    }

    @FXML
    public void onSearch(){
        isModified = false;

        String title = (searchTitleField != null && searchTitleField.getText() != null) ? searchTitleField.getText().trim() : "";
        String author = (searchAuthorField != null && searchAuthorField.getText() != null) ? searchAuthorField.getText().trim() : "";
        String isbn = (searchIsbnField != null && searchIsbnField.getText() != null) ? searchIsbnField.getText().trim() : "";
        String genre = (genreFilterComboBox != null && genreFilterComboBox.getValue() != null) ? genreFilterComboBox.getValue().trim() : "";

        if (title.isEmpty() && author.isEmpty() && isbn.isEmpty() && (genre.equals("Összes") || genre.isEmpty())) {
            System.out.println("Kérlek, adj meg legalább egy keresési feltételt!");
            return;
        }

        var results = repo.search(
                title.isEmpty() ? null : title,
                author.isEmpty() ? null : author,
                (genre.equals("Összes") || genre.isEmpty()) ? null : genre,
                isbn.isEmpty() ? null : isbn
        );

        bookList.setItems(FXCollections.observableList(results));
        resetButton.setVisible(true);
    }

    @FXML
    public void onSave(){
        isModified = false;
        repo.saveBooks(filePath);

    }

    @FXML
    public void onExportPdf() {
        try {
            PdfExporter.export(repo.getBooks(), "konyvlista.pdf");
            showAlert("Siker", "A könyvlista PDF-be exportálva (konyvlista.pdf).");
        } catch (Exception e) {
            showAlert("Hiba", "Nem sikerült exportálni: " + e.getMessage());
        }
    }


    @FXML
    public void onToggleBorrowed(){
        Book selected = bookList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Nincs kijelölés", "Válassz ki egy könyvet a kölcsönzéshez!");
            return;
        }

        if(!selected.isBorrowed()){
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Kölcsönözés");
            dialog.setHeaderText("Add meg a kölcsönző nevét!");
            dialog.setContentText("Név:");

            dialog.showAndWait().ifPresent(name -> {
                selected.setBorrowed(true);
                selected.setBorrowedBy(name);
                selected.setBorrowedDate(java.time.LocalDate.now().toString());
                selected.setDueDate(java.time.LocalDate.now().plusDays(14).toString());
                selected.setReturnedDate(null);
            });
        }else{
            selected.setBorrowed(false);
            selected.setReturnedDate(java.time.LocalDate.now().toString());
            long daysLate = selected.getDaysLate();
            if(daysLate > 0){
                showAlert("Késés", "A könyv " + daysLate + " nappal később lett visszahozva.");
            }
            selected.setBorrowedBy(null);
            selected.setBorrowedDate(null);
            selected.setDueDate(null);
        }

        refreshList();
    }

    @FXML
    public void onGenreFilter() {
        if (suppressComboBoxEvents) return;
        String selectedGenre = genreFilterComboBox.getValue();
        if (selectedGenre == null || selectedGenre.isEmpty() || selectedGenre.equals("Összes")) {
            refreshList();
        } else {
            List<Book> filtered = repo.getBooks().stream()
                    .filter(b -> selectedGenre.equalsIgnoreCase(b.getGenre()))
                    .toList();
            bookList.setItems(FXCollections.observableList(filtered));
        }
    }

    private boolean checkUnsavedChanges(){
        if(!isModified) return true;

        ButtonType saveAndExit = new ButtonType("Mentés");
        ButtonType exit = new ButtonType("Kilépés");
        ButtonType cancel = new ButtonType("Mégsem", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert= new Alert(Alert.AlertType.CONFIRMATION, "Változtatások nem lettek mentve. Mit szeretnél tenni?", saveAndExit, exit, cancel);
        alert.setTitle("Mentetlen változtatások");
        alert.setHeaderText(null);

        var result = alert.showAndWait();
        if(result.isEmpty() || result.get() == cancel){
            return false;
        } else if (result.get() == saveAndExit) {
            onSave();
            return true;
        }else {
            return true;
        }
    }

    /*private void refreshList(){
        bookList.setItems(FXCollections.observableList(repo.getBooks()));
        Set<String> genres = repo.getBooks().stream()
                .map(Book::getGenre)
                .filter(g -> g != null && !g.isEmpty())
                .collect(Collectors.toSet());
        genreFilterComboBox.setItems(FXCollections.observableArrayList(genres));
    }*/

    public void refreshList(){
        suppressComboBoxEvents = true;
        bookList.setItems(FXCollections.observableList(repo.getBooks()));

        Set<String> genres = repo.getBooks().stream()
                .map(Book::getGenre)
                .filter(g -> g != null && !g.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new)); // rendezett lista

        List<String> genreList = new ArrayList<>();
        genreList.add("Összes"); // ezt mindig elsőnek tesszük
        genreList.addAll(genres);

        genreFilterComboBox.setItems(FXCollections.observableArrayList(genreList));
        genreFilterComboBox.setValue("Összes"); // alapértelmezett érték

        suppressComboBoxEvents = false;
    }


    private void clearFields(){
        isModified = false;
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        genreComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String msg){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
