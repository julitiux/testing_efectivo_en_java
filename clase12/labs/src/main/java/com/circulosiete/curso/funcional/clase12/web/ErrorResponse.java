package com.circulosiete.curso.funcional.clase12.web;

public record ErrorResponse<T>(
    String message,
    String errorCode,
    T details
) {
}
