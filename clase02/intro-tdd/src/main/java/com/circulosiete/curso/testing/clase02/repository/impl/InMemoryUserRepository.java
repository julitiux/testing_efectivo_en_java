package com.circulosiete.curso.testing.clase02.repository.impl;


import com.circulosiete.curso.testing.clase02.model.User;
import com.circulosiete.curso.testing.clase02.repository.UserRepository;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean existsById(String id) {
        return users.containsKey(id);
    }

    @Override
    public void delete(String id) {
        users.remove(id);
    }

    @Override
    public int count() {
        return users.size();
    }
}
