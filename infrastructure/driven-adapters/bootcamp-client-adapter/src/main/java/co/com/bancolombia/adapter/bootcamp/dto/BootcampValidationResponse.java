package co.com.bancolombia.adapter.bootcamp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * BootcampValidationResponse - Respuesta de validación de bootcamps
 *
 * @param allExist Si todos los bootcamps existen
 * @param existingIds Lista de IDs de bootcamps que existen
 * @param hasDateConflicts Si hay conflictos de fechas entre los bootcamps
 * @param notFoundIds Lista de IDs que no fueron encontrados
 */
public record BootcampValidationResponse(
    @JsonProperty("allExist")
    Boolean allExist,

    @JsonProperty("existingIds")
    List<Long> existingIds,

    @JsonProperty("hasDateConflicts")
    Boolean hasDateConflicts,

    @JsonProperty("notFoundIds")
    List<Long> notFoundIds
) {}
