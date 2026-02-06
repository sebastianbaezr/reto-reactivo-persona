package co.com.bancolombia.api.dto.response;

public record PersonSummaryResponse(
    String name,
    String lastname,
    String email
) {}
