package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.PersonRequest;
import co.com.bancolombia.api.mapper.PersonMapper;
import co.com.bancolombia.api.validation.RequestValidationService;
import co.com.bancolombia.usecase.listpersonsbybootcamp.ListPersonsByBootcampUseCase;
import co.com.bancolombia.usecase.registerperson.RegisterPersonUseCase;
import co.com.bancolombia.usecase.getbootcampwithmostpeople.GetBootcampWithMostPeopleUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonHandler {

    private final RegisterPersonUseCase registerPersonUseCase;
    private final ListPersonsByBootcampUseCase listPersonsByBootcampUseCase;
    private final GetBootcampWithMostPeopleUseCase getBootcampWithMostPeopleUseCase;
    private final PersonMapper personMapper;
    private final RequestValidationService validationService;

    public Mono<ServerResponse> registerPerson(ServerRequest request) {
        return request.bodyToMono(PersonRequest.class)
            .flatMap(validationService::validate)
            .map(personMapper::toDomain)
            .flatMap(registerPersonUseCase::execute)
            .map(personMapper::toResponse)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Person registered successfully"))
            .doOnError(e -> log.error("Error registering person", e));
    }

    public Mono<ServerResponse> listPersonsByBootcamp(ServerRequest request) {
        return Mono.fromCallable(() -> Long.parseLong(request.pathVariable("bootcampId")))
            .flatMapMany(listPersonsByBootcampUseCase::execute)
            .map(personMapper::toSummaryResponse)
            .collectList()
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Persons listed successfully"))
            .doOnError(e -> log.error("Error listing persons", e));
    }

    public Mono<ServerResponse> getBootcampWithMostPeople(ServerRequest request) {
        return getBootcampWithMostPeopleUseCase.execute()
            .map(personMapper::toEnrollmentResponse)
            .flatMap(response -> ServerResponse.ok().bodyValue(response))
            .doOnSuccess(v -> log.info("Bootcamp with most people fetched successfully"))
            .doOnError(e -> log.error("Error fetching bootcamp with most people", e));
    }
}
