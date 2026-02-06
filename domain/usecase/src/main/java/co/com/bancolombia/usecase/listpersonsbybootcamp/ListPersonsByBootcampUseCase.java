package co.com.bancolombia.usecase.listpersonsbybootcamp;

import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class ListPersonsByBootcampUseCase {

    private final PersonRepository personRepository;

    public Flux<Person> execute(Long bootcampId) {
        return personRepository.findByBootcampId(bootcampId);
    }
}
