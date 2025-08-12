package com.circulosiete.curso.testing.clase02.exception;

/**
 * Exception thrown when a book is not found in the library system.
 * <p>
 * This exception is typically used in scenarios where an operation
 * requires a book identified by its ISBN or other unique identifier,
 * but no matching book is found in the library's catalog or database.
 * <p>
 * Example use cases include:
 * - Attempting to borrow a book that does not exist.
 * - Searching for a book using an invalid or inexistent ISBN.
 * <p>
 * This exception extends {@code RuntimeException}, which means it is
 * unchecked and does not need to be explicitly declared in method signatures.
 */
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
