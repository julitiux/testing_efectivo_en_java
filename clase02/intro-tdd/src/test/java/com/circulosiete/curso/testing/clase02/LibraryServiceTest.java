package com.circulosiete.curso.testing.clase02;

import com.circulosiete.curso.testing.clase02.exception.BookNotAvailableException;
import com.circulosiete.curso.testing.clase02.exception.BookNotFoundException;
import com.circulosiete.curso.testing.clase02.model.Book;
import com.circulosiete.curso.testing.clase02.model.User;
import com.circulosiete.curso.testing.clase02.repository.impl.InMemoryBookRepository;
import com.circulosiete.curso.testing.clase02.repository.impl.InMemoryUserRepository;
import com.circulosiete.curso.testing.clase02.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LibraryServiceTest {

    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService(
                new InMemoryBookRepository(),
                new InMemoryUserRepository()
        );
    }

    @Test
    @DisplayName("Debe agregar un libro al catálogo")
    void shouldAddBookToCatalog() {
        Book book = new Book("1984", "George Orwell", "978-0451524935");

        libraryService.addBook(book);

        List<Book> books = libraryService.getAllBooks();
        assertThat(books).hasSize(1);
        assertThat(books).contains(book);
    }

    @Test
    @DisplayName("Debe registrar un usuario")
    void shouldRegisterUser() {
        User user = new User("1", "Juan Pérez", "juan@email.com");

        libraryService.registerUser(user);

        User foundUser = libraryService.findUserById("1");
        assertThat(foundUser).isEqualTo(user);
    }

    @Test
    @DisplayName("Debe permitir a un usuario tomar prestado un libro disponible")
    void shouldAllowUserToBorrowAvailableBook() {
        // Given
        Book book = new Book("1984", "George Orwell", "978-0451524935");
        User user = new User("1", "Juan Pérez", "juan@email.com");
        libraryService.addBook(book);
        libraryService.registerUser(user);

        // When
        libraryService.borrowBook("1", "978-0451524935");

        // Then
        assertThat(book.isAvailable()).isFalse();
        assertThat(user.getBorrowedBooks()).contains(book);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el libro no existe")
    void shouldThrowExceptionWhenBookDoesNotExist() {
        User user = new User("1", "Juan Pérez", "juan@email.com");
        libraryService.registerUser(user);

        assertThatThrownBy(() -> libraryService.borrowBook("1", "inexistent-isbn"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Libro no encontrado con ISBN: inexistent-isbn");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el libro no está disponible")
    void shouldThrowExceptionWhenBookIsNotAvailable() {
        // Given
        Book book = new Book("1984", "George Orwell", "978-0451524935");
        User user1 = new User("1", "Juan Pérez", "juan@email.com");
        User user2 = new User("2", "Ana García", "ana@email.com");

        libraryService.addBook(book);
        libraryService.registerUser(user1);
        libraryService.registerUser(user2);
        libraryService.borrowBook("1", "978-0451524935");

        // When & Then
        assertThatThrownBy(() -> libraryService.borrowBook("2", "978-0451524935"))
                .isInstanceOf(BookNotAvailableException.class)
                .hasMessage("El libro no está disponible: 1984");
    }

    @Test
    @DisplayName("Debe permitir devolver un libro prestado")
    void shouldAllowReturningBorrowedBook() {
        // Given
        Book book = new Book("1984", "George Orwell", "978-0451524935");
        User user = new User("1", "Juan Pérez", "juan@email.com");
        libraryService.addBook(book);
        libraryService.registerUser(user);
        libraryService.borrowBook("1", "978-0451524935");

        // When
        libraryService.returnBook("1", "978-0451524935");

        // Then
        assertThat(book.isAvailable()).isTrue();
        assertThat(user.getBorrowedBooks()).isEmpty();
    }

    @Test
    @DisplayName("Debe buscar libros por título")
    void shouldFindBooksByTitle() {
        Book book1 = new Book("1984", "George Orwell", "978-0451524935");
        Book book2 = new Book("1984 (Spanish Edition)", "George Orwell", "978-8499890944");
        Book book3 = new Book("Brave New World", "Aldous Huxley", "978-0060850524");

        libraryService.addBook(book1);
        libraryService.addBook(book2);
        libraryService.addBook(book3);

        List<Book> foundBooks = libraryService.searchBooksByTitle("1984");

        assertThat(foundBooks).hasSize(2);
        assertThat(foundBooks).containsExactlyInAnyOrder(book1, book2);
    }
}
