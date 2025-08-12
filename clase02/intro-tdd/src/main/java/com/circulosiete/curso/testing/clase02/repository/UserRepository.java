package com.circulosiete.curso.testing.clase02.repository;


import com.circulosiete.curso.testing.clase02.model.User;

import java.util.List;
import java.util.Optional;

/**
 * The UserRepository interface serves as a repository for managing {@link User} entities.
 * It provides methods to perform CRUD (Create, Read, Update, Delete) operations as well as
 * utility methods such as counting and checking for entity existence.
 */
public interface UserRepository {
    /**
     * Persists the given {@link User} entity to the repository. If the user already exists,
     * updates the existing record; otherwise, creates a new record.
     *
     * @param user the user entity to be saved or updated; must not be null
     * @throws IllegalArgumentException if the user object is null
     */
    void save(User user);

    /**
     * Retrieves a {@link User} entity by its unique identifier.
     *
     * @param id the unique identifier of the user to be retrieved; must not be null or empty
     * @return an {@link Optional} containing the found {@link User}, or an empty {@link Optional} if no user is found with the given identifier
     * @throws IllegalArgumentException if the given id is null or empty
     */
    Optional<User> findById(String id);

    /**
     * Retrieves all {@link User} entities from the repository.
     *
     * @return a list of all {@link User} entities present in the repository;
     * returns an empty list if no users are found.
     */
    List<User> findAll();

    /**
     * Checks if an entity with the given identifier exists in the repository.
     *
     * @param id the unique identifier to check for existence; must not be null or empty
     * @return true if an entity with the given identifier exists, false otherwise
     */
    boolean existsById(String id);

    /**
     * Deletes a {@link User} entity from the repository based on its unique identifier.
     * If no user with the given identifier exists, no action is taken.
     *
     * @param id the unique identifier of the {@link User} to delete; must not be null or empty.
     *           If the identifier is null or empty, an {@link IllegalArgumentException} is thrown.
     */
    void delete(String id);

    /**
     * Returns the total number of entities in the repository.
     *
     * @return the count of entities currently stored in the repository.
     */
    int count();
}
