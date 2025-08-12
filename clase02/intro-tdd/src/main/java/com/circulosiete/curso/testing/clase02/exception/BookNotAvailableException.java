package com.circulosiete.curso.testing.clase02.exception;

/**
 * Exception thrown when an attempt is made to borrow a book that is not available.
 * This can occur if the book is already borrowed by another user or has been marked
 * as unavailable in the library system.
 */
public class BookNotAvailableException extends RuntimeException {
    public BookNotAvailableException(String message) {
        super(message);
    }
}
