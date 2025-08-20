package com.circulosiete.curso.testing.clase07.app.user;

import com.circulosiete.curso.testing.clase07.app.dto.CreateUserRequest;
import com.circulosiete.curso.testing.clase07.app.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest req) {
        var u = service.create(req.name());
        return ResponseEntity.ok(new UserResponse(u.getId(), u.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        var u = service.get(id);
        return ResponseEntity.ok(new UserResponse(u.getId(), u.getName()));
    }
}
