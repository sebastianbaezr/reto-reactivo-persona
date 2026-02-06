package co.com.bancolombia.usecase.validator;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;

public class PersonValidator {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_LASTNAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 100;
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainErrorCode.PERSON_NAME_REQUIRED);
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(DomainErrorCode.INVALID_NAME_LENGTH);
        }
    }

    public static void validateLastname(String lastname) {
        if (lastname == null || lastname.isBlank()) {
            throw new BusinessException(DomainErrorCode.PERSON_LASTNAME_REQUIRED);
        }
        if (lastname.length() > MAX_LASTNAME_LENGTH) {
            throw new BusinessException(DomainErrorCode.INVALID_LASTNAME_LENGTH);
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(DomainErrorCode.PERSON_EMAIL_REQUIRED);
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new BusinessException(DomainErrorCode.INVALID_EMAIL_LENGTH);
        }
        if (!email.matches(EMAIL_PATTERN)) {
            throw new BusinessException(DomainErrorCode.INVALID_EMAIL_FORMAT);
        }
    }
}
