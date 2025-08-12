package com.circulosiete.curso.testing.clase02.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserTest {
    @Test
    @DisplayName("Debe crear un usuario con ID, nombre y email")
    void shouldCreateUserWithIdNameAndEmail() {
        User user = new User("1", "Juan Pérez", "juan@email.com");

        assertThat(user.getId()).isEqualTo("1");
        assertThat(user.getName()).isEqualTo("Juan Pérez");
        assertThat(user.getEmail()).isEqualTo("juan@email.com");
        assertThat(user.getBorrowedBooks()).isEmpty();
    }

    @Test
    @DisplayName("Debe permitir al usuario tomar prestado un libro")
    void shouldAllowUserToBorrowBook() {
        User user = new User("1", "Juan Pérez", "juan@email.com");
        Book book = new Book("1984", "George Orwell", "978-0451524935");

        user.borrowBook(book);

        assertThat(user.getBorrowedBooks()).hasSize(1);
        assertThat(user.getBorrowedBooks()).contains(book);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario intenta tomar más de 3 libros")
    void shouldThrowExceptionWhenBorrowingMoreThanThreeBooks() {
        User user = new User("1", "Juan Pérez", "juan@email.com");
        Book book1 = new Book("1984", "George Orwell", "978-0451524935");
        Book book2 = new Book("Brave New World", "Aldous Huxley", "978-0060850524");
        Book book3 = new Book("Fahrenheit 451", "Ray Bradbury", "978-1451673319");
        Book book4 = new Book("The Handmaid's Tale", "Margaret Atwood", "978-0385490818");

        user.borrowBook(book1);
        user.borrowBook(book2);
        user.borrowBook(book3);

        assertThatThrownBy(() -> user.borrowBook(book4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("El usuario no puede tener más de 3 libros prestados");
    }
}
