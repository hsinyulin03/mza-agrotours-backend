package com.mza_agrotours.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class EdadMaximaValidator implements ConstraintValidator<EdadMaxima, LocalDate> {

    private int maxAnios;

    @Override
    public void initialize(EdadMaxima constraintAnnotation) {
        this.maxAnios = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        if (fechaNacimiento == null) return true;
        return !fechaNacimiento.isBefore(LocalDate.now().minusYears(maxAnios));
    }
}