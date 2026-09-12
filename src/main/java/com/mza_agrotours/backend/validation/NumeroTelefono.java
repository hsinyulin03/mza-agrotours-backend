package com.mza_agrotours.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NumeroTelefonoValidator.class)
public @interface NumeroTelefono {
    /** true = exige formato E.164 (+código país). Para teléfonos que van a Firebase. */
    boolean estricto() default false;

    String message() default "El campo no es un número de teléfono válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
