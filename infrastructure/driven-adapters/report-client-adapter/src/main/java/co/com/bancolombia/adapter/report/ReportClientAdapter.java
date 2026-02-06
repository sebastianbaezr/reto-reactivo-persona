package co.com.bancolombia.adapter.report;

import co.com.bancolombia.model.report.gateways.ReportClient;
import co.com.bancolombia.webclient.config.WebClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReportClientAdapter - Adaptador HTTP para consumir el servicio de Reportes
 *
 * Implementa el gateway ReportClient usando WebClient reactivo.
 * Utiliza patrón Fire & Forget: no espera respuesta, solo notifica.
 *
 * Endpoint consumido:
 * POST /api/bootcamp-report/register-person?ids=1,2,3
 *
 * Patrón Fire & Forget:
 * - Se envía la notificación sin esperar respuesta
 * - No bloquea el flujo principal
 * - Se ejecuta en background
 */
@Repository
@Slf4j
public class ReportClientAdapter implements ReportClient {

    private static final String SERVICE_NAME = "report";
    private static final int TIMEOUT_SECONDS = 3;

    private final WebClient webClient;

    public ReportClientAdapter(WebClientFactory webClientFactory) {
        this.webClient = webClientFactory.getWebClient(SERVICE_NAME);
        log.info("[ReportClientAdapter] Initialized with service: {}", SERVICE_NAME);
    }

    @Override
    public Mono<Void> notifyBootcampRegistration(List<Long> bootcampIds) {
        return Mono.just(bootcampIds)
            .filter(this::isBootcampIdsValid)
            .map(this::buildBootcampIdsParam)
            .flatMap(this::callReportService)
            .onErrorResume(this::handleNotificationError);
    }

    private boolean isBootcampIdsValid(List<Long> bootcampIds) {
        if (bootcampIds == null || bootcampIds.isEmpty()) {
            log.debug("[ReportClientAdapter] No bootcamp IDs to notify");
            return false;
        }
        return true;
    }

    private String buildBootcampIdsParam(List<Long> bootcampIds) {
        return bootcampIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }

    private Mono<Void> callReportService(String idsParam) {
        log.debug("[ReportClientAdapter] Calling report service with bootcamp IDs: {}", idsParam);

        return webClient
            .post()
            .uri(uriBuilder -> uriBuilder
                .path("/api/bootcamp-report/register-person")
                .queryParam("ids", idsParam)
                .build())
            .retrieve()
            .bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .doOnSuccess(v -> logSuccessNotification(idsParam))
            .doOnError(e -> logErrorNotification(idsParam, e));
    }

    private Mono<Void> handleNotificationError(Throwable error) {
        log.warn("[ReportClientAdapter] Report notification failed, but continuing with main flow", error);
        return Mono.empty();
    }

    private void logSuccessNotification(String idsParam) {
        log.debug("[ReportClientAdapter] Successfully notified report service with bootcamp IDs: {}", idsParam);
    }

    private void logErrorNotification(String idsParam, Throwable error) {
        log.warn("[ReportClientAdapter] Failed to notify report service for bootcamp IDs {}: {}", idsParam, error.getMessage());
    }
}
