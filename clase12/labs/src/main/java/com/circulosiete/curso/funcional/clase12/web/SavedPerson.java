package com.circulosiete.curso.funcional.clase12.web;

import com.circulosiete.curso.funcional.clase12.mapper.PersonMapper;
import com.circulosiete.curso.funcional.clase12.persistence.PersonEntity;

public record SavedPerson(
    Long id,
    PersonCommand person
) {
    public static SavedPerson from(PersonEntity person) {
        var personCommand = PersonMapper.INSTANCE.entityToCommand(person);
        return new SavedPerson(person.getId(), personCommand);
    }
}
