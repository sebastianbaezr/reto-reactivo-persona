package co.com.bancolombia.usecase.registerperson;

import co.com.bancolombia.model.bootcamp.gateways.BootcampClient;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import co.com.bancolombia.model.report.gateways.ReportClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RegisterPersonUseCase {

    private final PersonRepository personRepository;
    private final BootcampClient bootcampClient;
    private final ReportClient reportClient;

    public Mono<Person> execute(Person person) {
        return Mono.just(person)
            .filterWhen(this::emailNotExists)
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.PERSON_EMAIL_ALREADY_EXISTS)))
            .filterWhen(this::bootcampsAreValid)
            .switchIfEmpty(Mono.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND)))
            .flatMap(this::persistPersonWithBootcamps)
            .doOnSuccess(savedPerson -> notifyReportAsync(savedPerson));
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
        var bootcampIds = personToSave.getBootcampIds();
        return personRepository.save(personToSave)
            .flatMap(savedPerson -> personRepository
                .savePersonBootcamps(savedPerson.getId(), bootcampIds)
                .thenReturn(savedPerson.toBuilder().bootcampIds(bootcampIds).build()));
    }

    private void notifyReportAsync(Person savedPerson) {
        reportClient.notifyBootcampRegistration(savedPerson.getBootcampIds())
            .doOnSuccess(v -> System.out.println("[RegisterPersonUseCase] Report notification completed for person ID: " + savedPerson.getId()))
            .doOnError(error -> System.err.println("[RegisterPersonUseCase] Report notification failed: " + error.getMessage()))
            .subscribe();
    }
}
