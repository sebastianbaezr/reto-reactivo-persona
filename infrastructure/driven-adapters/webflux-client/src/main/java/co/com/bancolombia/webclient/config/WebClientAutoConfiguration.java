package co.com.bancolombia.webclient.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClientAutoConfiguration - Auto-configuración del módulo webflux-client
 *
 * Proporciona:
 * - Carga automática de ExternalServicesProperties desde application.yaml
 * - Registro de WebClientFactory como Bean de Spring
 */
@Configuration
@ConditionalOnClass(WebClient.class)
@EnableConfigurationProperties(ExternalServicesProperties.class)
public class WebClientAutoConfiguration {

    /**
     * Crea el Bean WebClientFactory
     */
    @Bean
    public WebClientFactory webClientFactory(ExternalServicesProperties properties) {
        return new WebClientFactory(properties);
    }
}
