package com.circulosiete.curso.testing.clase02.service;

import com.circulosiete.curso.testing.clase02.model.Book;
import com.circulosiete.curso.testing.clase02.model.User;
import com.circulosiete.curso.testing.clase02.exception.BookNotAvailableException;
import com.circulosiete.curso.testing.clase02.exception.BookNotFoundException;
import com.circulosiete.curso.testing.clase02.exception.UserNotFoundException;
import com.circulosiete.curso.testing.clase02.repository.BookRepository;
import com.circulosiete.curso.testing.clase02.repository.UserRepository;

import java.util.List;

/**
 * The LibraryService class provides operations for managing a library system,
 * including handling books and users, as well as borrowing and returning books.
 * It serves as the main business logic layer, interacting with BookRepository
 * and UserRepository for data persistence and retrieval.
 */
public class LibraryService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public LibraryService(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public void addBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("El libro no puede ser nulo");
        }
        bookRepository.save(book);
    }

    public void registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        if (userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ID: " + user.getId());
        }
        userRepository.save(user);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    public Book findBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException("Libro no encontrado con ISBN: " + isbn));
    }

    public void borrowBook(String userId, String isbn) {
        User user = findUserById(userId);
        Book book = findBookByIsbn(isbn);

        if (!book.isAvailable()) {
            throw new BookNotAvailableException("El libro no está disponible: " + book.getTitle());
        }

        user.borrowBook(book);
        book.setAvailable(false);

        // Guardar cambios en los repositories
        userRepository.save(user);
        bookRepository.save(book);
    }

    public void returnBook(String userId, String isbn) {
        User user = findUserById(userId);
        Book book = findBookByIsbn(isbn);

        if (book.isAvailable()) {
            throw new IllegalStateException("El libro ya está disponible: " + book.getTitle());
        }

        if (!user.getBorrowedBooks().contains(book)) {
            throw new IllegalStateException("El usuario no tiene prestado este libro");
        }

        user.returnBook(book);
        book.setAvailable(true);

        // Guardar cambios en los repositories
        userRepository.save(user);
        bookRepository.save(book);
    }

    public List<Book> searchBooksByTitle(String titleQuery) {
        if (titleQuery == null || titleQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("La consulta de búsqueda no puede ser nula o vacía");
        }
        return bookRepository.findByTitleContaining(titleQuery);
    }

    public List<Book> searchBooksByAuthor(String authorQuery) {
        if (authorQuery == null || authorQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("La consulta de búsqueda no puede ser nula o vacía");
        }
        return bookRepository.findByAuthorContaining(authorQuery);
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailable(true);
    }

    public List<Book> getBorrowedBooks() {
        return bookRepository.findByAvailable(false);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public int getTotalBooks() {
        return bookRepository.count();
    }

    public int getTotalUsers() {
        return userRepository.count();
    }
}
