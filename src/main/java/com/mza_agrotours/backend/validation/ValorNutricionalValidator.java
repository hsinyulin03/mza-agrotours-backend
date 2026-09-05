package com.mza_agrotours.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValorNutricionalValidator
        implements ConstraintValidator<ValorNutricionalValido, String> {

    private static final List<String> CATEGORIAS_VALIDAS =
            List.of("alto", "medio", "bajo");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {

        if (valor == null || valor.isBlank()) {
            return true;
        }

        String valorNormalizado = valor.trim().toLowerCase();

        if (CATEGORIAS_VALIDAS.contains(valorNormalizado)) {
            return true;
        }

        try {
            double numero = Double.parseDouble(valor.trim());

            if (numero < 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "El valor no puede ser negativo"
                ).addConstraintViolation();

                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}