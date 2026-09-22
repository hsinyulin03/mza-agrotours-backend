package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.clients.openweather.OpenWeatherClient;
import com.mza_agrotours.backend.dtos.clima.PronosticoSlot;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.clima.ClimaDptoDia;
import com.mza_agrotours.backend.enums.CondicionClima;
import com.mza_agrotours.backend.repositories.ClimaRepository;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenWeather devuelve slots de 3hs y nosotros guardamos un registro por dia, asi que toda la
 * logica interesante vive en la agregacion: que dia se arma, cual se descarta por venir cortado
 * y que condicion gana. Estos tests fijan ese contrato con el cliente HTTP mockeado, que es lo
 * unico verificable sin pegarle a la API real.
 */
@Transactional
class ClimaPronosticoIT extends AbstractIntegrationTest {

    private static final ZoneId ZONA = ZoneId.of(ClimaService.ZONA_ID);

    @Autowired
    private ClimaService climaService;

    @Autowired
    private ClimaRepository climaRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @MockitoBean
    private OpenWeatherClient openWeatherClient;

    private LocalDate hoy;
    private Departamento departamento;

    @BeforeEach
    void setUp() {
        this.hoy = LocalDate.now(ZONA);
        this.departamento = this.departamentoRepository.findAll().get(0);
        Mockito.when(this.openWeatherClient.obtenerPronostico(Mockito.anyDouble(), Mockito.anyDouble()))
                .thenReturn(this.pronosticoDeTresDias());
    }

    @Test
    void dadoPronosticoDeVariosDias_cuandoSeActualiza_entoncesDescartaElDiaFuturoIncompleto() {
        this.climaService.actualizarPronosticos();

        assertThat(this.porFecha()).containsOnlyKeys(this.hoy, this.hoy.plusDays(1));
    }

    @Test
    void dadoDiaCompleto_cuandoSeActualiza_entoncesAgregaTemperaturasYProbabilidadDeLluvia() {
        this.climaService.actualizarPronosticos();

        ClimaDptoDia manana = this.porFecha().get(this.hoy.plusDays(1));
        assertThat(manana.getTemperaturaMax()).isEqualTo(33.0);
        assertThat(manana.getTemperaturaMin()).isEqualTo(9.0);
        assertThat(manana.getTemperaturaMed()).isEqualTo(20.0);
        assertThat(manana.getProbabilidadLluvia()).isEqualTo(60.0);
    }

    @Test
    void dadoDiaConVariasCondiciones_cuandoSeActualiza_entoncesGuardaLaDeMayorPrioridad() {
        this.climaService.actualizarPronosticos();

        assertThat(this.porFecha().get(this.hoy.plusDays(1)).getCondicion())
                .isEqualTo(CondicionClima.THUNDERSTORM);
    }

    @Test
    void dadoPronosticoYaGuardado_cuandoSeVuelveAActualizar_entoncesNoDuplicaElDia() {
        this.climaService.actualizarPronosticos();
        this.climaService.actualizarPronosticos();

        assertThat(this.climaRepository.findByDepartamentoOrderByFecha(this.departamento)).hasSize(2);
    }

    private Map<LocalDate, ClimaDptoDia> porFecha() {
        return this.climaRepository.findByDepartamentoOrderByFecha(this.departamento).stream()
                .collect(Collectors.toMap(ClimaDptoDia::getFecha, Function.identity()));
    }

    private List<PronosticoSlot> pronosticoDeTresDias() {
        List<PronosticoSlot> slots = new ArrayList<>();

        slots.add(slot(this.hoy, 5.0, 6.0, 4.0, 5.0, CondicionClima.MIST));
        slots.add(slot(this.hoy, 7.0, 8.0, 6.0, 15.0, CondicionClima.CLEAR_SKY));

        LocalDate manana = this.hoy.plusDays(1);
        slots.add(slot(manana, 10.0, 11.0, 9.0, 10.0, CondicionClima.CLEAR_SKY));
        slots.add(slot(manana, 20.0, 22.0, 18.0, 60.0, CondicionClima.RAIN));
        slots.add(slot(manana, 30.0, 33.0, 28.0, 40.0, CondicionClima.THUNDERSTORM));
        slots.add(slot(manana, 15.0, 16.0, 14.0, 0.0, CondicionClima.CLEAR_SKY));
        slots.add(slot(manana, 25.0, 26.0, 24.0, 20.0, CondicionClima.CLEAR_SKY));

        LocalDate pasado = this.hoy.plusDays(2);
        slots.add(slot(pasado, 12.0, 13.0, 11.0, 0.0, CondicionClima.CLEAR_SKY));
        slots.add(slot(pasado, 14.0, 15.0, 13.0, 0.0, CondicionClima.CLEAR_SKY));

        return slots;
    }

    private static PronosticoSlot slot(LocalDate fecha, Double temp, Double max, Double min,
                                       Double pop, CondicionClima condicion) {
        return new PronosticoSlot(fecha, temp, max, min, pop, condicion);
    }
}
