package co.com.bancolombia.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainErrorCode {
    PERSON_EMAIL_ALREADY_EXISTS("PERSON_EMAIL_ALREADY_EXISTS", "Ya existe una persona con este email"),
    INVALID_NAME_LENGTH("INVALID_NAME_LENGTH", "El nombre no debe exceder 50 caracteres"),
    INVALID_LASTNAME_LENGTH("INVALID_LASTNAME_LENGTH", "El apellido no debe exceder 50 caracteres"),
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", "El formato del email es inválido"),
    INVALID_EMAIL_LENGTH("INVALID_EMAIL_LENGTH", "El email no debe exceder 100 caracteres"),
    PERSON_NAME_REQUIRED("PERSON_NAME_REQUIRED", "El nombre es obligatorio"),
    PERSON_LASTNAME_REQUIRED("PERSON_LASTNAME_REQUIRED", "El apellido es obligatorio"),
    PERSON_EMAIL_REQUIRED("PERSON_EMAIL_REQUIRED", "El email es obligatorio"),
    BOOTCAMP_IDS_REQUIRED("BOOTCAMP_IDS_REQUIRED", "Debe proporcionar al menos un bootcamp"),
    MIN_BOOTCAMPS_REQUIRED("MIN_BOOTCAMPS_REQUIRED", "Debe proporcionar al menos 1 bootcamp"),
    MAX_BOOTCAMPS_EXCEEDED("MAX_BOOTCAMPS_EXCEEDED", "No puede proporcionar más de 4 bootcamps"),
    BOOTCAMPS_NOT_FOUND("BOOTCAMPS_NOT_FOUND", "Uno o más bootcamps no existen"),
    BOOTCAMPS_DATE_CONFLICT("BOOTCAMPS_DATE_CONFLICT", "Los bootcamps seleccionados tienen conflictos de fechas");

    private final String code;
    private final String message;
}
