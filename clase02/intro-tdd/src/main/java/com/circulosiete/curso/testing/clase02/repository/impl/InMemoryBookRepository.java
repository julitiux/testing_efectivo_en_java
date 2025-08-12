package com.circulosiete.curso.testing.clase02.repository.impl;

import com.circulosiete.curso.testing.clase02.model.Book;
import com.circulosiete.curso.testing.clase02.repository.BookRepository;

import java.util.*;

public class InMemoryBookRepository implements BookRepository {
    private final Map<String, Book> books = new HashMap<>();

    @Override
    public void save(Book book) {
        books.put(book.getIsbn(), book);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(books.get(isbn));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    @Override
    public List<Book> findByTitleContaining(String title) {
        String titleLower = title.toLowerCase();
        return books.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(titleLower))
                .toList();
    }

    @Override
    public List<Book> findByAuthorContaining(String author) {
        String authorLower = author.toLowerCase();
        return books.values().stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(authorLower))
                .toList();
    }

    @Override
    public List<Book> findByAvailable(boolean available) {
        return books.values().stream()
                .filter(book -> book.isAvailable() == available)
                .toList();
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return books.containsKey(isbn);
    }

    @Override
    public void delete(String isbn) {
        books.remove(isbn);
    }

    @Override
    public int count() {
        return books.size();
    }
}
