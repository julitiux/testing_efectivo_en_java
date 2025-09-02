package com.circulosiete.curso.funcional.clase12.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.ZonedDateTime;

public record PersonCommand(
    @NotEmpty
    String firstName,
    @NotEmpty
    String lastName,
    @NotNull
    @Email
    String email,
    @NotNull
    @Past
    ZonedDateTime birthDate
) {
}
