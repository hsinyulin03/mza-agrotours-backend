package com.mza_agrotours.backend.validation;

import jakarta.validation.ConstraintValidator;
import java.util.regex.Pattern;

public class NumeroTelefonoValidator implements ConstraintValidator<NumeroTelefono, String> {
    private static final String REGEX = "^[0-9+()\\-\\s]{7,16}$";

    // E.164: "+", código de país sin 0 inicial, 8 a 15 dígitos en total.
    private static final Pattern ESTRICTO = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private boolean estricto;


    @Override
    public void initialize(NumeroTelefono anotacion) {
        this.estricto = anotacion.estricto();
    }

    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        if (estricto) {
            return ESTRICTO.matcher(value).matches();
        }

        return value.matches(REGEX);
    }
}
