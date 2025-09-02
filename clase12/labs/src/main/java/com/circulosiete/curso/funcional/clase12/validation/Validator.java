package com.circulosiete.curso.funcional.clase12.validation;

import com.circulosiete.curso.funcional.errorhandling.Failure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Validator {
    private static final ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
    private static final jakarta.validation.Validator _validator = factory.getValidator();

    public static <T> Validation<Failure<String, ConstraintViolation<T>>, T> validate(T object) {
        return Option.of(object)
            .map(o -> Tuple.of(o, _validator.validate(o)))
            .map(data -> buildValidationErrorFailure(data._1, data._2))
            .getOrElse(() -> getInvalid("Invalid object because is null."));
    }

    @SuppressWarnings("unchecked")
    private static <T> Validation<Failure<String, ConstraintViolation<T>>, T> getInvalid(String message) {
        return Validation.invalid(
            (Failure<String, ConstraintViolation<T>>)
                Failure.of(message)
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> Failure<ConstraintViolation<T>, Void> toFailure(
        ConstraintViolation<T> constraintViolation
    ) {
        return (Failure<ConstraintViolation<T>, Void>)
            Failure.of(constraintViolation.getMessage(), constraintViolation);
    }

    private static <T> Validation<Failure<String, ConstraintViolation<T>>, T> buildValidationErrorFailure(
        T object,
        Set<ConstraintViolation<T>> constraintViolations
    ) {
        if (constraintViolations.isEmpty()) {
            return Validation.valid(object);
        }
        final var violations = constraintViolations.stream()
            .map(Validator::toFailure)
            .toList();

        return Validation.invalid(
            Failure.of(
                "The provided object contains violations.",
                "",
                violations
            )
        );
    }

    protected static <T> Failure<String, String> createFailureFromViolation(
        Failure<String, ConstraintViolation<T>> violation
    ) {
        final var details = violation.details()
            .map(failures -> failures.stream()
                .map(Validator::getFailure)
                .toList())
            .orElse(List.of());

        return Failure.fromBrokenBusinessRules(
            violation.message(),
            "validation_error",
            details,
            Optional.of(violation.message())
        );
    }

    private static <T> Failure<String, Void> getFailure(
        Failure<ConstraintViolation<T>, Void> failure
    ) {
        return new Failure<String, Void>(
            "",
            null,
            null,
            null,
            null,
            null,
            null,
            Optional.of(failure.errorData().toString())
        );
    }

    public static <T> Either<Failure<String, String>, T> validateObject(T object) {
        return Validator.validate(object)
            .toEither()
            .mapLeft(Validator::createFailureFromViolation);
    }
}
