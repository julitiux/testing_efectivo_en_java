package com.circulosiete.curso.funcional.clase12.web;

import com.circulosiete.curso.funcional.clase12.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/people")
@RequiredArgsConstructor
public class PersonController {
    private final PersonService personService;

    @PostMapping("/")
    public ResponseEntity<?> save(
        @RequestBody PersonCommand personCommand
    ) {
        return ResponseFactory.from(
            personService.save(personCommand)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
        @PathVariable Long id
    ) {
        return ResponseFactory.from(
            personService.findById(id)
        );
    }
}
