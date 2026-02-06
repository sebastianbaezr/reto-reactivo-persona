package co.com.bancolombia.webclient.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebClientFactory - Factory genérico para crear y cachear WebClients
 *
 * Responsabilidades:
 * - Crear WebClients configurados dinámicamente según application.yaml
 * - Cachear WebClients para reutilización
 * - Aplicar timeouts y configuraciones de red
 *
 * Ventajas:
 * - Reutilizable en múltiples microservicios
 * - Configuración centralizada en YAML
 * - Cacheo de clientes evita recrearlos constantemente
 * - Thread-safe con ConcurrentHashMap
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebClientFactory {

    private final ExternalServicesProperties properties;
    private final Map<String, WebClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Obtiene o crea un WebClient para el servicio especificado.
     *
     * Los WebClients se cachean después de su creación para ser reutilizados.
     *
     * @param serviceName Nombre del servicio (ej: "bootcamp")
     * @return WebClient configurado para el servicio
     * @throws IllegalArgumentException si el servicio no está configurado en application.yaml
     */
    public WebClient getWebClient(String serviceName) {
        return clientCache.computeIfAbsent(serviceName, this::createWebClient);
    }

    /**
     * Crea un nuevo WebClient con configuración específica del servicio
     *
     * Configura:
     * - Base URL desde properties
     * - Connect timeout (milisegundos)
     * - Response timeout (segundos)
     * - Headers por defecto (Content-Type: application/json)
     */
    private WebClient createWebClient(String serviceName) {
        log.info("[WebClientFactory] Creating WebClient for service: {}", serviceName);

        ServiceProperties serviceProps = properties.getService(serviceName);

        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(serviceProps.responseTimeout()))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, serviceProps.connectTimeout());

        WebClient webClient = WebClient.builder()
            .baseUrl(serviceProps.baseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();

        log.debug("[WebClientFactory] WebClient created for service: {} with baseUrl: {}",
            serviceName, serviceProps.baseUrl());

        return webClient;
    }

    /**
     * Limpia el cache de WebClients
     *
     * Útil para testing o cuando se necesita reiniciar los clientes
     */
    public void clearCache() {
        log.debug("[WebClientFactory] Clearing WebClient cache");
        clientCache.clear();
    }
}
