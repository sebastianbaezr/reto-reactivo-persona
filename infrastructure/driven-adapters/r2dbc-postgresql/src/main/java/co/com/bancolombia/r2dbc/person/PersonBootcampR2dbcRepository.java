package co.com.bancolombia.r2dbc.person;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PersonBootcampR2dbcRepository extends ReactiveCrudRepository<PersonBootcampData, Long> {

    @Query("INSERT INTO person_bootcamps (person_id, bootcamp_id) VALUES (:personId, :bootcampId)")
    Mono<Void> saveRelation(@Param("personId") Long personId, @Param("bootcampId") Long bootcampId);
}
