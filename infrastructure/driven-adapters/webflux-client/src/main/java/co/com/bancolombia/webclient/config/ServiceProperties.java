package co.com.bancolombia.webclient.config;

/**
 * ServiceProperties - Configuración de un servicio externo
 *
 * @param baseUrl Base URL del servicio (ej: http://bootcamp-service:8080)
 * @param connectTimeout Timeout de conexión en milisegundos (ms)
 * @param responseTimeout Timeout de respuesta en segundos (s)
 */
public record ServiceProperties(
    String baseUrl,
    Integer connectTimeout,
    Integer responseTimeout
) {}
