package com.circulosiete.curso.testing.clase02.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private final String id;
    private final String name;
    private final String email;
    private final List<Book> borrowedBooks;
    private static final int MAX_BORROWED_BOOKS = 3;

    public User(String id, String name, String email) {
        validateId(id);
        validateName(name);
        validateEmail(email);

        this.id = id;
        this.name = name;
        this.email = email;
        this.borrowedBooks = new ArrayList<>();
    }

    public void borrowBook(Book book) {
        if (borrowedBooks.size() >= MAX_BORROWED_BOOKS) {
            throw new IllegalStateException("El usuario no puede tener más de 3 libros prestados");
        }
        borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        borrowedBooks.remove(book);
    }

    // Validaciones y getters...
    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser nulo o vacío");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o vacío");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede ser nulo o vacío");
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<Book> getBorrowedBooks() { return new ArrayList<>(borrowedBooks); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
