package com.circulosiete.curso.testing.clase02.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BookTest {
    @Test
    @DisplayName("Debe crear un libro con título, autor e ISBN")
    void shouldCreateBookWithTitleAuthorAndIsbn() {
        // When
        Book book = new Book("1984", "George Orwell", "978-0451524935");

        // Then
        assertThat(book.getTitle()).isEqualTo("1984");
        assertThat(book.getAuthor()).isEqualTo("George Orwell");
        assertThat(book.getIsbn()).isEqualTo("978-0451524935");
        assertThat(book.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el título es nulo")
    void shouldThrowExceptionWhenTitleIsNull() {
        assertThatThrownBy(() -> new Book(null, "George Orwell", "978-0451524935"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El título no puede ser nulo o vacío");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el título está vacío")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        assertThatThrownBy(() -> new Book("", "George Orwell", "978-0451524935"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El título no puede ser nulo o vacío");
    }
}
