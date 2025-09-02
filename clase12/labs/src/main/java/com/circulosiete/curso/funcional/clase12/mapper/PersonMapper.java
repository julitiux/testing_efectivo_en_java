package com.circulosiete.curso.funcional.clase12.mapper;

import com.circulosiete.curso.funcional.clase12.persistence.PersonEntity;
import com.circulosiete.curso.funcional.clase12.web.PersonCommand;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PersonMapper {

    PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);

    PersonEntity commandToEntity(PersonCommand command);

    PersonCommand entityToCommand(PersonEntity entity);
}
