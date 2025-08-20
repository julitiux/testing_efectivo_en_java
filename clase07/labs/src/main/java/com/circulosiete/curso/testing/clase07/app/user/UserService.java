package com.circulosiete.curso.testing.clase07.app.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User create(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        User u = new User(name);
        return repo.save(u);
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("not found"));
    }
}
