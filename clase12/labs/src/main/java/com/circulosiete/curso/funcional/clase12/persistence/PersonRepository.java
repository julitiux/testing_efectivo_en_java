package com.circulosiete.curso.funcional.clase12.persistence;

import com.circulosiete.curso.funcional.errorhandling.Failure;
import io.vavr.control.Either;
import org.springframework.data.repository.ListCrudRepository;

public interface PersonRepository extends ListCrudRepository<PersonEntity, Long> {

    default Either<Failure<String, String>, PersonEntity> persist(PersonEntity persona) {
        return CrudRepositoryErrorHandling.save(this, persona);
    }
}
