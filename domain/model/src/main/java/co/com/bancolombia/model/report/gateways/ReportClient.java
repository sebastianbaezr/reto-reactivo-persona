package co.com.bancolombia.model.report.gateways;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway para comunicación con el microservicio de Reportes.
 * Notifica al servicio de reportes cuando una persona se registra a bootcamps.
 */
public interface ReportClient {

    /**
     * Notifica al servicio de reportes que una persona se registró a varios bootcamps.
     *
     * @param bootcampIds Lista de IDs de bootcamps donde se registró la persona
     * @return Mono<Void> para operación fire & forget
     */
    Mono<Void> notifyBootcampRegistration(List<Long> bootcampIds);
}
