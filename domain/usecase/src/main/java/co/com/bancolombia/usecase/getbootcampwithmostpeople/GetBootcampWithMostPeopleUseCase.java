package co.com.bancolombia.usecase.getbootcampwithmostpeople;

import co.com.bancolombia.model.person.BootcampEnrollment;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetBootcampWithMostPeopleUseCase {

    private final PersonRepository personRepository;

    public Mono<BootcampEnrollment> execute() {
        return personRepository.findBootcampWithMostPeople();
    }
}
