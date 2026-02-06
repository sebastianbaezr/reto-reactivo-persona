package co.com.bancolombia.usecase.registerperson;

import co.com.bancolombia.model.bootcamp.gateways.BootcampClient;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RegisterPersonUseCase {

    private final PersonRepository personRepository;
    private final BootcampClient bootcampClient;

    public Mono<Person> execute(Person person) {
        return Mono.just(person)
            .filterWhen(this::emailNotExists)
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.PERSON_EMAIL_ALREADY_EXISTS)))
            .filterWhen(this::bootcampsAreValid)
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND)))
            .flatMap(this::persistPersonWithBootcamps);
    }

    private Mono<Boolean> emailNotExists(Person person) {
        return personRepository.existsByEmail(person.getEmail())
            .map(exists -> !exists);
    }

    private Mono<Boolean> bootcampsAreValid(Person person) {
        return bootcampClient.validateBootcamps(person.getBootcampIds())
            .all(isValid -> isValid);
    }

    private Mono<Person> persistPersonWithBootcamps(Person personToSave) {
        return personRepository.save(personToSave)
            .flatMap(savedPerson -> personRepository
                .savePersonBootcamps(savedPerson.getId(), personToSave.getBootcampIds())
                .thenReturn(savedPerson));
    }
}
