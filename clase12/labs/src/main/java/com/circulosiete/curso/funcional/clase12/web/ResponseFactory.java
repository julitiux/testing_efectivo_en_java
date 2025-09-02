package com.circulosiete.curso.funcional.clase12.web;

import com.circulosiete.curso.funcional.errorhandling.Failure;
import io.vavr.control.Either;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseFactory {
    public static <T> ResponseEntity<?> from(Either<Failure<String, String>, T> response) {
        return response.fold(
            ResponseFactory::handleFailure,
            ResponseEntity::ok
        );
    }

    private static ResponseEntity<?> handleFailure(Failure<String, String> failure) {
        var errorResponse = new ErrorResponse<>(
            failure.message(),
            failure.errorCode()
                .orElse(""),
            failure.details().orElse(List.of())
        );
        var errorCode = failure
            .errorCode()
            .orElse("unknown");

        return switch (errorCode) {
            case "validation_error" -> unprocessableEntity(errorResponse);
            case "illegal_argument_exception" -> badRequest(errorResponse);
            case "not_found" -> notFound(errorResponse);
            default -> ResponseEntity.internalServerError().body(errorResponse);
        };
    }

    private static ResponseEntity<ErrorResponse<List<Failure<String, Void>>>> notFound(
        ErrorResponse<List<Failure<String, Void>>> errorResponse
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
    }

    private static ResponseEntity<ErrorResponse<List<Failure<String, Void>>>> badRequest(
        ErrorResponse<List<Failure<String, Void>>> errorResponse
    ) {
        return ResponseEntity
            .badRequest()
            .body(errorResponse);
    }

    private static ResponseEntity<ErrorResponse<List<Failure<String, Void>>>> unprocessableEntity(
        ErrorResponse<List<Failure<String, Void>>> errorResponse
    ) {
        return ResponseEntity
            .unprocessableEntity()
            .body(errorResponse);
    }
}
