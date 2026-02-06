package co.com.bancolombia.api.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestValidationService {

    private final Validator validator;

    public <T> Mono<T>  validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    ConstraintViolation::getMessage
                ));
            return Mono.error(new ValidationException(errors));
        }

        return Mono.just(request);
    }
}
