package com.mza_agrotours.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EdadMaximaValidator.class)
public @interface EdadMaxima {

    int value() default 120;

    String message() default "La fecha no puede ser de hace más de {value} años";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}