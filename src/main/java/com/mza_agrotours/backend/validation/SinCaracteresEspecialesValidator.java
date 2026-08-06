package com.mza_agrotours.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SinCaracteresEspecialesValidator implements ConstraintValidator<SinCaracteresEspeciales, String> {

    // Expresión regular: Letras, números, acentos, la ñ, espacios, guion bajo y guion medio.
    private static final String REGEX = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ _-]*$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        return value.matches(REGEX);
    }
}
