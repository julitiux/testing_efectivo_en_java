package com.circulosiete.curso.testing.clase02.model;

/**
 * The Book class represents a book entity with attributes such as title, author,
 * ISBN, and availability status.
 * <p>
 * Instances of this class are immutable except for the availability status,
 * which can be updated after creation.
 */
public class Book {
    private final String title;
    private final String author;
    private final String isbn;
    private boolean available;

    public Book(String title, String author, String isbn) {
        validateTitle(title);
        validateAuthor(author);
        validateIsbn(isbn);

        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede ser nulo o vacío");
        }
    }

    private void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor no puede ser nulo o vacío");
        }
    }

    private void validateIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("El ISBN no puede ser nulo o vacío");
        }
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
