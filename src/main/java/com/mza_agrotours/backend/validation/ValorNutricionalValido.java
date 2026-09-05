package com.mza_agrotours.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValorNutricionalValidator.class)
public @interface ValorNutricionalValido {

    String message() default "El valor debe ser un número o una categoría válida (alto, medio, bajo)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}