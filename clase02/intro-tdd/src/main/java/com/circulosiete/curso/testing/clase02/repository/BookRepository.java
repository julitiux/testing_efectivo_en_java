package com.circulosiete.curso.testing.clase02.repository;

import com.circulosiete.curso.testing.clase02.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * The BookRepository interface provides a contract for managing Book entities
 * within a repository. It defines methods for creating, retrieving, updating,
 * and deleting books as well as querying books based on various criteria.
 */
public interface BookRepository {
    /**
     * Saves a given book entity to the repository.
     *
     * @param book the book entity to be saved. Must not be null.
     *             The book should contain valid title, author, and ISBN fields.
     */
    void save(Book book);

    /**
     * Retrieves a book entity from the repository based on its ISBN.
     *
     * @param isbn the ISBN of the book to retrieve. Must not be null or empty.
     * @return an Optional containing the Book if found, or an empty Optional if no book with the given ISBN exists.
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Retrieves all books available in the repository.
     *
     * @return a list containing all Book entities in the repository. If no books exist, returns an empty list.
     */
    List<Book> findAll();

    /**
     * Retrieves a list of books whose titles contain the specified substring.
     *
     * @param title the substring to search for in book titles. Must not be null or empty.
     * @return a list of books that contain the specified substring in their titles. If no such books exist, returns an empty list.
     */
    List<Book> findByTitleContaining(String title);

    /**
     * Retrieves a list of books whose authors contain the specified substring.
     *
     * @param author the substring to search for in book authors. Must not be null or empty.
     * @return a list of books that contain the specified substring in their authors. If no such books exist, returns an empty list.
     */
    List<Book> findByAuthorContaining(String author);

    /**
     * Retrieves a list of books based on their availability status.
     *
     * @param available the availability status of the books to retrieve.
     *                  If true, only available books will be returned.
     *                  If false, only unavailable books will be returned.
     * @return a list of books that match the specified availability status.
     * If no books match, returns an empty list.
     */
    List<Book> findByAvailable(boolean available);

    /**
     * Checks if a book with the specified ISBN exists in the repository.
     *
     * @param isbn the ISBN of the book to check for existence. Must not be null or empty.
     * @return true if a book with the given ISBN exists, false otherwise.
     */
    boolean existsByIsbn(String isbn);

    /**
     * Deletes a book from the repository based on its ISBN.
     *
     * @param isbn the ISBN of the book to delete. Must not be null or empty.
     *             If no book with the given ISBN exists, no action is taken.
     */
    void delete(String isbn);

    /**
     * Returns the total number of books in the repository.
     *
     * @return the count of books currently stored in the repository.
     */
    int count();
}
