package com.mza_agrotours.backend.validation;

import com.mza_agrotours.backend.dtos.UsuarioUpdateReq;
import com.mza_agrotours.backend.dtos.establecimiento.DTOEstablecimientoAlta;
import com.mza_agrotours.backend.dtos.receta.DTORecetaAM;
import com.mza_agrotours.backend.dtos.roles_permisos.RolUpdateRequest;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateReq;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoAM;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El set de caracteres que acepta este validador es una decisión de negocio, no un detalle de
 * implementación: define qué nombres reales puede cargar un productor sin que el alta rebote.
 * Los tests fijan esa frontera en sus dos bordes discutidos: la diéresis, que apellidos como
 * Argüello necesitan, y el punto, que es la razón por la que razonSocial quedó fuera del alcance
 * de la anotación. La segunda mitad verifica que la anotación siga puesta en los campos titulares.
 */
class SinCaracteresEspecialesValidatorTest {

    private static final String MENSAJE = "El campo contiene caracteres especiales no permitidos";

    private static ValidatorFactory factory;
    private static Validator beanValidator;

    private final SinCaracteresEspecialesValidator validador = new SinCaracteresEspecialesValidator();

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
            "Argüello",
            "MALARGÜE",
            "Finca San José",
            "Ñandú del Oeste",
            "Cosecha 2025 - Malbec",
            "Bodega_Sur"
    })
    void aceptaNombresPropiosDelDominio(String valor) {
        assertThat(validador.isValid(valor, null)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void delegaLosValoresVaciosEnNotBlank(String valor) {
        assertThat(validador.isValid(valor, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<script>alert(1)</script>",
            "Robert'); DROP TABLE usuarios;--",
            "Bodega@Sur",
            "Finca \"El Cisne\"",
            "Tour 50% descuento",
            "Agro del Sur S.R.L."
    })
    void rechazaCaracteresEspeciales(String valor) {
        assertThat(validador.isValid(valor, null)).isFalse();
    }

    @Test
    void anotaElNombreDeUsuario() {
        UsuarioUpdateReq req = new UsuarioUpdateReq();
        req.setNombre("Juan@Pérez");

        assertThat(rechazaPorCaracteresEspeciales(req, "nombre")).isTrue();
    }

    @Test
    void anotaElNombreDeEstablecimiento() {
        DTOEstablecimientoAlta req = new DTOEstablecimientoAlta();
        req.setNombre("Finca #1");

        assertThat(rechazaPorCaracteresEspeciales(req, "nombre")).isTrue();
    }

    @Test
    void anotaElNombreDeEstablecimientoEnLaSolicitud() {
        SolicitudEstablecimientoCreateReq req = new SolicitudEstablecimientoCreateReq();
        req.setNombreEstablecimiento("Finca #1");

        assertThat(rechazaPorCaracteresEspeciales(req, "nombreEstablecimiento")).isTrue();
    }

    @Test
    void anotaElNombreDeRol() {
        RolUpdateRequest req = new RolUpdateRequest();
        req.setNombre("Gestor@Fincas");

        assertThat(rechazaPorCaracteresEspeciales(req, "nombre")).isTrue();
    }

    @Test
    void anotaElNombreDeTipoCultivo() {
        DTOTipoCultivoAM req = new DTOTipoCultivoAM();
        req.setNombre("Uva <Malbec>");

        assertThat(rechazaPorCaracteresEspeciales(req, "nombre")).isTrue();
    }

    @Test
    void anotaElNombreDeReceta() {
        DTORecetaAM req = new DTORecetaAM();
        req.setNombre("Dulce de uva 100%");

        assertThat(rechazaPorCaracteresEspeciales(req, "nombre")).isTrue();
    }

    @Test
    void dejaLaRazonSocialFueraDelAlcance() {
        DTOEstablecimientoAlta req = new DTOEstablecimientoAlta();
        req.setRazonSocial("Agro del Sur S.R.L.");

        assertThat(rechazaPorCaracteresEspeciales(req, "razonSocial")).isFalse();
    }

    private boolean rechazaPorCaracteresEspeciales(Object dto, String propiedad) {
        return beanValidator.validate(dto).stream()
                .anyMatch(violacion -> propiedad.equals(violacion.getPropertyPath().toString())
                        && MENSAJE.equals(violacion.getMessage()));
    }
}
