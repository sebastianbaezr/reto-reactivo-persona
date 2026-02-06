package co.com.bancolombia.model.person.gateways;

import co.com.bancolombia.model.person.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public interface PersonRepository {
    Mono<Person> save(Person person);

    Mono<Boolean> existsByEmail(String email);

    Mono<Person> findById(Long id);

    Flux<Person> findAll();

    Mono<Void> savePersonBootcamps(Long personId, List<Long> bootcampIds);
}
