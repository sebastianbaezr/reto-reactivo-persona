package co.com.bancolombia.webclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * ExternalServicesProperties - Configuración multi-servicio
 *
 * Mapea la sección 'external-services' del application.yaml con todas las configuraciones
 * de servicios externos disponibles.
 *
 * Ejemplo en application.yaml:
 * ```yaml
 * external-services:
 *   services:
 *     bootcamp:
 *       base-url: "http://bootcamp-service:8080"
 *       connect-timeout: 3000
 *       response-timeout: 5
 * ```
 */
@ConfigurationProperties(prefix = "external-services")
public record ExternalServicesProperties(
    Map<String, ServiceProperties> services
) {
    /**
     * Constructor compacto - inicializa services vacío si no está configurado
     */
    public ExternalServicesProperties {
        if (services == null) {
            services = Map.of();
        }
    }

    /**
     * Obtiene la configuración de un servicio específico
     *
     * @param serviceName Nombre del servicio (ej: "bootcamp")
     * @return ServiceProperties configuración del servicio
     * @throws IllegalArgumentException si el servicio no está configurado
     */
    public ServiceProperties getService(String serviceName) {
        ServiceProperties props = services.get(serviceName);
        if (props == null) {
            throw new IllegalArgumentException(
                "Service configuration not found: " + serviceName);
        }
        return props;
    }
}
