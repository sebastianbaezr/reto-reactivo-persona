package co.com.bancolombia.r2dbc.person;

import co.com.bancolombia.model.person.Person;
import co.com.bancolombia.model.person.gateways.PersonRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@Repository
public class PersonRepositoryAdapter extends ReactiveAdapterOperations<Person, PersonData, Long, PersonR2dbcRepository>
        implements PersonRepository {

    private final PersonBootcampR2dbcRepository personBootcampRepository;

    public PersonRepositoryAdapter(
            PersonR2dbcRepository repository,
            PersonBootcampR2dbcRepository personBootcampRepository,
            ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Person.class));
        this.personBootcampRepository = personBootcampRepository;
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email)
            .doOnError(e -> log.error("[PersonRepositoryAdapter] Error checking email existence: {}", email, e));
    }

    @Override
    public Mono<Void> savePersonBootcamps(Long personId, List<Long> bootcampIds) {
        return isBootcampIdsValid(bootcampIds)
            .flatMap(valid -> saveBootcampRelations(personId, bootcampIds))
            .doOnSuccess(v -> log.info("[PersonRepositoryAdapter] Bootcamp relations saved for person: {}", personId))
            .doOnError(e -> log.error("[PersonRepositoryAdapter] Error saving bootcamp relations for person: {}", personId, e));
    }

    private Mono<Boolean> isBootcampIdsValid(List<Long> bootcampIds) {
        if (bootcampIds == null || bootcampIds.isEmpty()) {
            log.debug("[PersonRepositoryAdapter] Bootcamp IDs are empty or null, skipping save");
            return Mono.empty();
        }
        return Mono.just(true);
    }

    private Mono<Void> saveBootcampRelations(Long personId, List<Long> bootcampIds) {
        return Flux.fromIterable(bootcampIds)
            .flatMap(bootcampId -> saveIndividualBootcampRelation(personId, bootcampId))
            .then();
    }

    private Mono<Void> saveIndividualBootcampRelation(Long personId, Long bootcampId) {
        return personBootcampRepository.saveRelation(personId, bootcampId)
            .doOnSuccess(v -> log.debug("[PersonRepositoryAdapter] Saved bootcamp relation - person: {}, bootcamp: {}",
                personId, bootcampId));
    }
}
