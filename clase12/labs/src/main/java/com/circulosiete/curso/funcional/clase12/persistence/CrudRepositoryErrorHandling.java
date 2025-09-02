package com.circulosiete.curso.funcional.clase12.persistence;

import com.circulosiete.curso.funcional.clase12.validation.Validator;
import com.circulosiete.curso.funcional.errorhandling.Failure;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.repository.CrudRepository;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

public class CrudRepositoryErrorHandling {

    public static <T, ID> Either<Failure<String, String>, T> save(
        CrudRepository<T, ID> repository, T entity
    ) {
        return Validator.validateObject(entity)
            .flatMap(validEntity -> CrudRepositoryErrorHandling.internalSave(repository, validEntity));
    }

    private static <T, ID> Either<Failure<String, String>, T> internalSave(
        CrudRepository<T, ID> repository, T entity
    ) {
        return Try.of(() -> repository.save(entity))
            .toEither()
            .mapLeft(CrudRepositoryErrorHandling::of);
    }


    public static Failure<String, String> of(Throwable throwable) {
        return Match(throwable)
            .of(
                Case(
                    $(instanceOf(OptimisticLockingFailureException.class)),
                    CrudRepositoryErrorHandling::handleOptimisticLockingFailureException
                ),
                Case(
                    $(instanceOf(SQLException.class)),
                    CrudRepositoryErrorHandling::handleSqlException
                ),
                Case(
                    $(instanceOf(IllegalArgumentException.class)),
                    CrudRepositoryErrorHandling::handleIllegalArgumentException
                ),
                Case(
                    $(instanceOf(RuntimeException.class)),
                    CrudRepositoryErrorHandling::handleRuntimeException
                ),
                Case(
                    $(),
                    CrudRepositoryErrorHandling::handleUnknownException
                )
            );
    }

    public static Failure<String, String> handleOptimisticLockingFailureException(OptimisticLockingFailureException e) {
        return createFailureInstance(
            "Error desconocido",
            e.getMessage(),
            "optimistic_locking_failure",
            e
        );
    }

    public static Failure<String, String> handleIllegalArgumentException(IllegalArgumentException e) {
        return createFailureInstance(
            "Parámetros incorrectos",
            e.getMessage(),
            "illegal_argument_exception",
            e
        );
    }


    public static Failure<String, String> handleUnknownException(Throwable e) {
        return createFailureInstance(
            "Error desconocido",
            e.getMessage(),
            "unknown",
            e
        );
    }

    public static Failure<String, String> handleRuntimeException(RuntimeException e) {
        return createFailureInstance(
            "Error de runtime",
            e.getMessage(),
            "runtime",
            e
        );
    }


    public static Failure<String, String> handleSqlException(SQLException e) {
        return createFailureInstance(
            "Error de SQL",
            e.getMessage(),
            "general_sql_error",
            e
        );
    }

    public static Failure<String, String> createFailureInstance(
        String message,
        String reason,
        String errorCode,
        Throwable cause
    ) {
        return new Failure<String, String>(
            message,
            Optional.of(reason),
            Optional.of(errorCode),
            Optional.empty(),
            Optional.empty(),
            Optional.of(cause),
            Optional.empty(),
            Optional.empty()
        );
    }


}
