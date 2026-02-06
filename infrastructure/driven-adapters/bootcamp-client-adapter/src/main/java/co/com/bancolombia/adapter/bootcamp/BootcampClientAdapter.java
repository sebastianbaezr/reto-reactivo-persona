package co.com.bancolombia.adapter.bootcamp;

import co.com.bancolombia.adapter.bootcamp.dto.BootcampValidationRequest;
import co.com.bancolombia.adapter.bootcamp.dto.BootcampValidationResponse;
import co.com.bancolombia.model.bootcamp.gateways.BootcampClient;
import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Repository
@Slf4j
public class BootcampClientAdapter implements BootcampClient {

    private static final String SERVICE_NAME = "bootcamp";
    private static final String VALIDATE_PATH = "/api/bootcamps/validate";
    private static final int TIMEOUT_SECONDS = 5;

    private final WebClient webClient;

    public BootcampClientAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
        log.info("[BootcampClientAdapter] Initialized with service: {}", SERVICE_NAME);
    }

    @Override
    public Mono<Boolean> existsBootcamp(Long bootcampId) {
        log.debug("[BootcampClientAdapter] Checking existence of bootcamp: {}", bootcampId);

        return validateBootcamps(List.of(bootcampId))
            .next()
            .defaultIfEmpty(false);
    }

    @Override
    public Flux<Boolean> validateBootcamps(List<Long> bootcampIds) {
        log.debug("[BootcampClientAdapter] Validating {} bootcamps", bootcampIds.size());

        return callBootcampValidationService(bootcampIds)
            .flatMapMany(this::processValidationResponse)
            .doOnError(e -> log.error("[BootcampClientAdapter] Error validating bootcamps", e));
    }

    private Mono<BootcampValidationResponse> callBootcampValidationService(List<Long> bootcampIds) {
        BootcampValidationRequest request = new BootcampValidationRequest(bootcampIds);

        log.debug("[BootcampClientAdapter] Calling validate endpoint with {} bootcamp IDs", bootcampIds.size());

        return webClient
            .post()
            .uri(VALIDATE_PATH)
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
            .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
            .bodyToMono(BootcampValidationResponse.class)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .doOnSuccess(response -> log.debug(
                "[BootcampClientAdapter] Validation response - allExist: {}, hasDateConflicts: {}",
                response.allExist(), response.hasDateConflicts()))
            .onErrorResume(this::handleRequestError);
    }


    private Flux<Boolean> processValidationResponse(BootcampValidationResponse response) {
        log.debug("[BootcampClientAdapter] Processing validation response - allExist: {}, hasDateConflicts: {}",
            response.allExist(), response.hasDateConflicts());

        if (!Boolean.TRUE.equals(response.allExist())) {
            log.error("[BootcampClientAdapter] Some bootcamps not found: {}", response.notFoundIds());
            return Flux.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND));
        }

        if (Boolean.TRUE.equals(response.hasDateConflicts())) {
            log.error("[BootcampClientAdapter] Date conflicts detected in bootcamps");
            return Flux.error(new BusinessException(DomainErrorCode.BOOTCAMPS_DATE_CONFLICT));
        }

        log.debug("[BootcampClientAdapter] All bootcamps are valid - no conflicts");
        return Flux.fromIterable(response.existingIds())
            .map(id -> true)
            .doOnComplete(() -> log.debug("[BootcampClientAdapter] Validation flux completed successfully"));
    }

    private Mono<Throwable> handleClientError(ClientResponse response) {
        log.warn("[BootcampClientAdapter] Client error from bootcamp service: {}", response.statusCode());
        return Mono.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND));
    }

    private Mono<Throwable> handleServerError(ClientResponse response) {
        log.error("[BootcampClientAdapter] Server error from bootcamp service: {}", response.statusCode());
        return Mono.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND));
    }

    private Mono<BootcampValidationResponse> handleRequestError(Throwable error) {
        log.error("[BootcampClientAdapter] Error communicating with bootcamp service", error);

        if (error instanceof WebClientRequestException) {
            return Mono.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND));
        }
        return Mono.error(error);
    }
}
