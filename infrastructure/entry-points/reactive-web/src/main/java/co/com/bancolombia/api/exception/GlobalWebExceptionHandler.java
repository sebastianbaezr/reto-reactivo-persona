package co.com.bancolombia.api.exception;

import co.com.bancolombia.api.dto.response.ErrorResponse;
import co.com.bancolombia.api.validation.ValidationException;
import co.com.bancolombia.model.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(-2)
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private static final String LOGGER_PREFIX = "[GlobalWebExceptionHandler]";

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("{} Exception occurred: {}", LOGGER_PREFIX, ex.getMessage(), ex);

        if (ex instanceof ValidationException) {
            return handleValidationException(exchange, (ValidationException) ex);
        }

        if (ex instanceof WebExchangeBindException) {
            return handleValidationException(exchange, (WebExchangeBindException) ex);
        }

        if (ex instanceof BusinessException) {
            return handleBusinessException(exchange, (BusinessException) ex);
        }

        return handleGenericException(exchange, ex);
    }

    private Mono<Void> handleValidationException(ServerWebExchange exchange, ValidationException ex) {
        log.warn("{} Validation error: {}", LOGGER_PREFIX, ex.getMessage());

        List<Map<String, String>> errors = ex.getErrors().entrySet()
            .stream()
            .map(entry -> Map.of(
                "field", entry.getKey(),
                "message", entry.getValue()
            ))
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Validation failed")
            .timestamp(System.currentTimeMillis())
            .details(errors.isEmpty() ? new ArrayList<>() : new ArrayList<>(errors))
            .build();

        return writeValidationResponse(exchange, errorResponse);
    }

    private Mono<Void> handleValidationException(ServerWebExchange exchange, WebExchangeBindException ex) {
        log.warn("{} Validation error: {}", LOGGER_PREFIX, ex.getMessage());

        List<Map<String, String>> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> Map.of(
                "field", error.getField(),
                "message", error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
            ))
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Validation failed")
            .timestamp(System.currentTimeMillis())
            .details(errors.isEmpty() ? new ArrayList<>() : new ArrayList<>(errors))
            .build();

        return writeValidationResponse(exchange, errorResponse);
    }

    private Mono<Void> handleBusinessException(ServerWebExchange exchange, BusinessException ex) {
        HttpStatus status = determineStatusFromBusinessException(ex);
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getCode())
            .message(ex.getMessage())
            .timestamp(System.currentTimeMillis())
            .details(new ArrayList<>())
            .build();

        return writeResponse(exchange, status, errorResponse);
    }

    private Mono<Void> handleGenericException(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String code = "INTERNAL_ERROR";
        String message = "Error interno del servidor";

        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .details(new ArrayList<>())
            .build();

        return writeResponse(exchange, status, errorResponse);
    }

    private HttpStatus determineStatusFromBusinessException(BusinessException ex) {
        return switch (ex.getCode()) {
            case "TECHNOLOGY_NAME_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private Mono<Void> writeValidationResponse(ServerWebExchange exchange, ErrorResponse errorResponse) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonResponse = mapper.writeValueAsString(errorResponse);
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes()))
            );
        } catch (Exception e) {
            log.error("{} Error writing validation response", LOGGER_PREFIX, e);
            return Mono.empty();
        }
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, ErrorResponse errorResponse) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonResponse = convertToJsonString(errorResponse);
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes()))
            );
        } catch (Exception e) {
            log.error("{} Error writing response", LOGGER_PREFIX, e);
            return Mono.empty();
        }
    }

    private String convertToJsonString(ErrorResponse errorResponse) {
        return String.format(
            "{\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":%d,\"details\":[]}",
            escapeJson(errorResponse.getCode()),
            escapeJson(errorResponse.getMessage()),
            errorResponse.getTimestamp()
        );
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
