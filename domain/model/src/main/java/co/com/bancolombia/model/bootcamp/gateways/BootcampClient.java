package co.com.bancolombia.model.bootcamp.gateways;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public interface BootcampClient {
    Mono<Boolean> existsBootcamp(Long bootcampId);

    Flux<Boolean> validateBootcamps(List<Long> bootcampIds);
}
