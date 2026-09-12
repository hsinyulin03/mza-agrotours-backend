package com.mza_agrotours.backend.validation;

import jakarta.validation.ConstraintValidator;

public class NumeroTelefonoValidator implements ConstraintValidator<NumeroTelefono, String> {
    private static final String REGEX = "^[0-9+()\\-\\s]{7,16}$";

    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        return value.matches(REGEX);
    }
}
