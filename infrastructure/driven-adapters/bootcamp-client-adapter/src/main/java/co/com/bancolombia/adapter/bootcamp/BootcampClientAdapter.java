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

/**
 * BootcampClientAdapter - Adaptador HTTP para consumir el servicio de Bootcamps
 *
 * Implementa el gateway BootcampClient usando WebClient reactivo.
 *
 * Endpoint consumido:
 * POST /api/bootcamps/validate
 * Request: { "ids": [1, 2, 3] }
 * Response: {
 *   "allExist": true,
 *   "existingIds": [1, 2, 3],
 *   "hasDateConflicts": false,
 *   "notFoundIds": []
 * }
 *
 * Validación:
 * - Retorna true por cada bootcamp si:
 *   1. El bootcamp existe (está en existingIds)
 *   2. NO hay conflictos de fechas (hasDateConflicts es false)
 *
 * Responsabilidades:
 * - Comunicar con el microservicio de Bootcamps
 * - Validar existencia y disponibilidad (sin conflictos) de bootcamps
 * - Manejar errores de conexión y timeout
 * - Loguear interacciones para debugging
 *
 * Principios:
 * - 100% reactivo con Mono/Flux
 * - Sin try/catch (delegación a GlobalExceptionHandler)
 * - Logging explícito para trazabilidad
 * - Timeouts configurables
 */
@Repository
@Slf4j
public class BootcampClientAdapter implements BootcampClient {

    private static final String SERVICE_NAME = "bootcamp";
    private static final String VALIDATE_PATH = "/api/bootcamps/validate";
    private static final int TIMEOUT_SECONDS = 5;

    private final WebClient webClient;

    /**
     * Constructor que inyecta el WebClientFactory genérico
     *
     * @param webClientFactory Factory que proporciona el WebClient configurado
     */
    public BootcampClientAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
        log.info("[BootcampClientAdapter] Initialized with service: {}", SERVICE_NAME);
    }

    /**
     * Valida la existencia de un bootcamp individual
     *
     * @param bootcampId ID del bootcamp a validar
     * @return Mono<Boolean> true si existe y no hay conflictos, false si no existe
     */
    @Override
    public Mono<Boolean> existsBootcamp(Long bootcampId) {
        log.debug("[BootcampClientAdapter] Checking existence of bootcamp: {}", bootcampId);

        return validateBootcamps(List.of(bootcampId))
            .next()
            .defaultIfEmpty(false);
    }

    /**
     * Valida la existencia de múltiples bootcamps de forma reactiva
     *
     * Flujo:
     * 1. Llamar endpoint POST con todos los bootcamp IDs
     * 2. Recibir respuesta con allExist, hasDateConflicts, existingIds
     * 3. Retornar Flux<Boolean> donde cada Boolean es true si:
     *    - El bootcamp existe (está en existingIds)
     *    - Y NO hay conflictos de fechas
     *
     * Ejemplo:
     * - Request: [1, 2, 3]
     * - Response: allExist=true, hasDateConflicts=false, existingIds=[1,2,3]
     * - Retorna: Flux con [true, true, true]
     *
     * @param bootcampIds Lista de IDs a validar
     * @return Flux<Boolean> donde cada Boolean indica si el bootcamp es válido
     * @throws BusinessException si no puede conectar al servicio o hay error
     */
    @Override
    public Flux<Boolean> validateBootcamps(List<Long> bootcampIds) {
        log.debug("[BootcampClientAdapter] Validating {} bootcamps", bootcampIds.size());

        return callBootcampValidationService(bootcampIds)
            .flatMapMany(this::processValidationResponse)
            .doOnError(e -> log.error("[BootcampClientAdapter] Error validating bootcamps", e));
    }

    /**
     * Llama al servicio de bootcamps para validar los IDs
     *
     * @param bootcampIds Lista de IDs a validar
     * @return Mono con la respuesta del servicio
     */
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

    /**
     * Procesa la respuesta del servicio de bootcamps
     *
     * Validación con lanzamiento de excepciones:
     * - Si allExist es false → lanza BOOTCAMPS_NOT_FOUND
     * - Si allExist es true pero hasDateConflicts es true → lanza BOOTCAMPS_DATE_CONFLICT
     * - Si todo es válido → retorna Flux con true para cada bootcamp
     *
     * @param response Respuesta del servicio
     * @return Flux<Boolean> con true para cada bootcamp si todo es válido
     * @throws BusinessException si hay errores de validación
     */
    private Flux<Boolean> processValidationResponse(BootcampValidationResponse response) {
        log.debug("[BootcampClientAdapter] Processing validation response - allExist: {}, hasDateConflicts: {}",
            response.allExist(), response.hasDateConflicts());

        // Caso 1: Algunos bootcamps no existen
        if (!Boolean.TRUE.equals(response.allExist())) {
            log.error("[BootcampClientAdapter] Some bootcamps not found: {}", response.notFoundIds());
            return Flux.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND));
        }

        // Caso 2: Todos existen pero hay conflictos de fechas
        if (Boolean.TRUE.equals(response.hasDateConflicts())) {
            log.error("[BootcampClientAdapter] Date conflicts detected in bootcamps");
            return Flux.error(new BusinessException(DomainErrorCode.BOOTCAMPS_DATE_CONFLICT));
        }

        // Caso 3: Todo válido - retornar true para cada bootcamp
        log.debug("[BootcampClientAdapter] All bootcamps are valid - no conflicts");
        return Flux.fromIterable(response.existingIds())
            .map(id -> true)
            .doOnComplete(() -> log.debug("[BootcampClientAdapter] Validation flux completed successfully"));
    }

    /**
     * Maneja errores 4xx del servicio de bootcamps
     */
    private Mono<Throwable> handleClientError(ClientResponse response) {
        log.warn("[BootcampClientAdapter] Client error from bootcamp service: {}", response.statusCode());
        return Mono.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND));
    }

    /**
     * Maneja errores 5xx del servicio de bootcamps
     */
    private Mono<Throwable> handleServerError(ClientResponse response) {
        log.error("[BootcampClientAdapter] Server error from bootcamp service: {}", response.statusCode());
        return Mono.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND));
    }

    /**
     * Maneja errores de comunicación (timeout, conexión, etc)
     */
    private Mono<BootcampValidationResponse> handleRequestError(Throwable error) {
        log.error("[BootcampClientAdapter] Error communicating with bootcamp service", error);

        if (error instanceof WebClientRequestException) {
            return Mono.error(new BusinessException(DomainErrorCode.BOOTCAMPS_NOT_FOUND));
        }
        return Mono.error(error);
    }
}
