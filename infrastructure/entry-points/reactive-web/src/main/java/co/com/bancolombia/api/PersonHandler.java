package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.PersonRequest;
import co.com.bancolombia.api.mapper.PersonMapper;
import co.com.bancolombia.api.validation.RequestValidationService;
import co.com.bancolombia.usecase.registerperson.RegisterPersonUseCase;
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
    private final PersonMapper personMapper;
    private final RequestValidationService validationService;

    public Mono<ServerResponse> registerPerson(ServerRequest request) {
        return request.bodyToMono(PersonRequest.class)
            .flatMap(validationService::validate)
            .map(personMapper::toEntity)
            .flatMap(registerPersonUseCase::execute)
            .map(personMapper::toResponse)
            .flatMap(response -> ServerResponse.status(201).bodyValue(response))
            .doOnSuccess(v -> log.info("Person registered successfully"))
            .doOnError(e -> log.error("Error registering person", e));
    }
}
