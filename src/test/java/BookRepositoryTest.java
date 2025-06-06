import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import org.example.controller.LibraryController;
import org.example.model.Book;
import org.example.model.BookRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BookRepositoryTest {
    private LibraryController controller;


    @BeforeAll
    public static void initToolkit() {
        new JFXPanel();
    }

    @BeforeEach
    public void setUp() {
        controller = new LibraryController();

        controller.titleField = new TextField();
        controller.authorField = new TextField();
        controller.isbnField = new TextField();
        controller.genreComboBox = new ComboBox<>();
        controller.genreFilterComboBox = new ComboBox<>();
        controller.bookList = new ListView<>();
        controller.addButton = new Button();

        controller.filterComboBox = controller.genreFilterComboBox;

        controller.repo = new BookRepository();

        controller.genreComboBox.setItems(FXCollections.observableArrayList(
                "Regény", "Krimi", "Fantasy"
        ));
    }

    @Test
    public void testAddBookSuccessfully() {
        controller.titleField.setText("1984");
        controller.authorField.setText("George Orwell");
        controller.isbnField.setText("1234567890");
        controller.genreComboBox.setValue("Regény");

        controller.onAdd();

        List<Book> books = controller.repo.getBooks();
        assertEquals(1, books.size());
        assertEquals("1984", books.get(0).getTitle());
    }

    @Test
    public void testAddBookFailsOnMissingField() {
        controller.titleField.setText("1984");
        controller.authorField.setText("");  // Missing author
        controller.isbnField.setText("1234567890");
        controller.genreComboBox.setValue("Regény");

        controller.onAdd();

        List<Book> books = controller.repo.getBooks();
        assertTrue(books.isEmpty(), "A könyv nem kerülhet be, ha hiányzik mező.");
    }

    @Test
    public void testDuplicateIsbnIsRejected() {
        controller.repo.addBook(new Book("Book A", "Author A", "Regény", "1111"));

        controller.titleField.setText("Book B");
        controller.authorField.setText("Author B");
        controller.isbnField.setText("1111"); // Same ISBN
        controller.genreComboBox.setValue("Regény");

        controller.onAdd();

        assertEquals(1, controller.repo.getBooks().size(), "Ne engedjen be duplikált ISBN-t.");
    }

    @Test
    public void testDeleteBookRemovesFromRepo() {
        Book book = new Book("Book A", "Author A", "Krimi", "5555");
        controller.repo.addBook(book);
        controller.refreshList();

        controller.bookList.getSelectionModel().select(book);
        controller.onDelete();

        assertFalse(controller.repo.getBooks().contains(book));
    }

    @Test
    public void testEditBookUpdatesFields() {
        Book book = new Book("Old", "Writer", "Fantasy", "111");
        controller.repo.addBook(book);
        controller.refreshList();

        controller.bookList.getSelectionModel().select(book);
        controller.onEdit();

        controller.titleField.setText("New Title");
        controller.onSaveEdit(book);

        assertEquals("New Title", book.getTitle());
    }

    @Test
    public void testGenreFilterShowsCorrectBooks() {
        Book fantasyBook = new Book("Book A", "Author A", "Fantasy", "001");
        Book crimeBook = new Book("Book B", "Author B", "Krimi", "002");
        controller.repo.addBook(fantasyBook);
        controller.repo.addBook(crimeBook);

        controller.genreFilterComboBox.setItems(FXCollections.observableArrayList("Regény", "Krimi", "Fantasy"));
        controller.genreFilterComboBox.setValue("Fantasy");
        controller.onGenreFilter();

        List<Book> displayedBooks = controller.bookList.getItems();
        assertEquals(1, displayedBooks.size());
        assertEquals("Fantasy", displayedBooks.get(0).getGenre());
    }

    @Test
    public void testEditBookWithNoSelectionDoesNothing() {
        assertDoesNotThrow(() -> controller.onEdit());
    }

    @Test
    public void testClearFilterShowsAllBooks() {
        controller.repo.addBook(new Book("Book A", "Author A", "Fantasy", "001"));
        controller.repo.addBook(new Book("Book B", "Author B", "Krimi", "002"));

        controller.genreFilterComboBox.setValue(null); // no filter
        controller.onFilter();

        List<Book> displayedBooks = controller.bookList.getItems();
        assertEquals(2, displayedBooks.size());
    }

    @Test
    public void testToggleBorrowedSetsBorrowedFields() {
        Book book = new Book("Test könyv", "Teszt Elek", "Regény", "1234");
        controller.repo.addBook(book);
        controller.refreshList();

        controller.bookList.getSelectionModel().select(book);

        LibraryController testController = new LibraryController() {
            @Override
            protected TextInputDialog createNameInputDialog() {
                TextInputDialog mockDialog = mock(TextInputDialog.class);
                when(mockDialog.showAndWait()).thenReturn(Optional.of("Teszt Olvasó"));
                return mockDialog;
            }

            @Override
            public void refreshList() {}
        };

        testController.bookList = controller.bookList;

        testController.repo = controller.repo;

        testController.onToggleBorrowed();

        assertTrue(book.isBorrowed());
        assertEquals("Teszt Olvasó", book.getBorrowedBy());
        assertNotNull(book.getBorrowedDate());
        assertNotNull(book.getDueDate());
    }

    @Test
    public void testSortByTitleAscending() {
        Book bookA = new Book("A könyv", "Zsuzsa", "Regény", "001");
        Book bookC = new Book("C könyv", "Árpád", "Regény", "003");
        Book bookB = new Book("B könyv", "Béla", "Regény", "002");

        controller.repo.addBook(bookA);
        controller.repo.addBook(bookC);
        controller.repo.addBook(bookB);

        controller.bookList.setItems(FXCollections.observableArrayList(controller.repo.getBooks()));

        controller.sortComboBox = new ComboBox<>();
        controller.sortComboBox.setItems(FXCollections.observableArrayList(
                "Cím szerint (A-Z)", "Cím szerint (Z-A)",
                "Szerző szerint (A-Z)", "Szerző szerint (Z-A)"
        ));
        controller.sortComboBox.setValue("Cím szerint (A-Z)");

        controller.onSort();

        List<Book> sorted = controller.bookList.getItems();
        assertEquals("A könyv", sorted.get(0).getTitle());
        assertEquals("B könyv", sorted.get(1).getTitle());
        assertEquals("C könyv", sorted.get(2).getTitle());
    }
}
