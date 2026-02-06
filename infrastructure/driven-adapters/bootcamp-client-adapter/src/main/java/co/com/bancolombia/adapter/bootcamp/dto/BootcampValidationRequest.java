package co.com.bancolombia.adapter.bootcamp.dto;

import java.util.List;

/**
 * BootcampValidationRequest - Request para validar bootcamps
 *
 * @param ids Lista de IDs de bootcamps a validar
 */
public record BootcampValidationRequest(
    List<Long> ids
) {}
