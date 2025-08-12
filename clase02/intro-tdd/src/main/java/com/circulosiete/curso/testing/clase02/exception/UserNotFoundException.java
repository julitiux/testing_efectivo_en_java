package com.circulosiete.curso.testing.clase02.exception;

/**
 * Exception thrown when a user is not found in the system.
 * <p>
 * This exception is typically used in scenarios where an operation
 * requires a user identified by their unique identifier, such as a
 * username or user ID, but no corresponding user exists in the system.
 * <p>
 * The exception extends {@code RuntimeException}, making it unchecked
 * and not required to be explicitly declared in method signatures.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
