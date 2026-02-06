package co.com.bancolombia.r2dbc.person;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PersonR2dbcRepository extends ReactiveCrudRepository<PersonData, Long> {

    @Query("SELECT EXISTS(SELECT 1 FROM persons WHERE email = :email)")
    Mono<Boolean> existsByEmail(@Param("email") String email);
}
