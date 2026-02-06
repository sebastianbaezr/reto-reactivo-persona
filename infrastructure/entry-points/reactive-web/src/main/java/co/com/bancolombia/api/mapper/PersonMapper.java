package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.request.PersonRequest;
import co.com.bancolombia.api.dto.response.PersonResponse;
import co.com.bancolombia.model.person.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    @Mapping(target = "id", ignore = true)
    Person toEntity(PersonRequest request);

    PersonResponse toResponse(Person entity);
}
