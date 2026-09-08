package com.mza_agrotours.backend.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NumeroTelefonoValidatorTest {
    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static Validator beanValidator = factory.getValidator();

    private final NumeroTelefonoValidator validador = new NumeroTelefonoValidator();

    @BeforeAll
    static void abrirFactory() {
        factory = Validation.buildDefaultValidatorFactory();
        beanValidator = factory.getValidator();
    }

    @AfterAll
    static void cerrarFactory() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+5492616563974",
            "+54-261-656-3974",
            "+54 (261)6563974",
            "+234567",
            "+234567890123456"
    })
    void aceptaNumerosTelefonicosDelDominio(String numeroTelefono) {
        assertThat(validador.isValid(numeroTelefono, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+23",
            "+3242-hola",
            "+3743748737437437437473",
            "+3473743747holalaadofiajfjsf",
    })
    void rechazaNumerosTelefonicosFueraDelDominio(String numeroTelefono) {
        assertThat(validador.isValid(numeroTelefono, null)).isFalse();
    }

    private boolean rechazaNumeroTelefonoInvalido(Object dto, String propiedad) {
        return beanValidator.validate(dto).stream()
                .anyMatch(violacion -> propiedad.equals(violacion.getPropertyPath().toString()));
    }
}
