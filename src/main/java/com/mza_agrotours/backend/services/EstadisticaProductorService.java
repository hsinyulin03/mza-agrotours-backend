package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.estadisticasproductor.EstadisticasResponse;
import com.mza_agrotours.backend.dtos.estadisticasproductor.KpisDTO;
import com.mza_agrotours.backend.dtos.estadisticasproductor.PeriodoDTO;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstadisticaProductorService {

private final ReservaRepository reservaRepository;
private final ActividadRepository actividadRepository;

// Reservas que cuentan para las estadísticas: pagadas (día futuro) y finalizadas (día ya realizado)
private static final List<EstadoReservaNombre> ESTADOS_CONFIRMADOS =
        List.of(EstadoReservaNombre.PAGADA, EstadoReservaNombre.FINALIZADA);

// Reservas canceladas, con o sin reembolso
private static final List<EstadoReservaNombre> ESTADOS_CANCELADOS =
        List.of(EstadoReservaNombre.CANCELADA_CON_REEMBOLSO, EstadoReservaNombre.CANCELADA_SIN_REEMBOLSO);

// Reservas gestionadas: las que llegaron a pagarse (confirmadas) más las canceladas. Se excluyen PENDIENTE y EXPIRADA
private static final List<EstadoReservaNombre> ESTADOS_GESTIONADOS = List.of(
        EstadoReservaNombre.PAGADA, EstadoReservaNombre.FINALIZADA,
        EstadoReservaNombre.CANCELADA_CON_REEMBOLSO, EstadoReservaNombre.CANCELADA_SIN_REEMBOLSO);

@Transactional(readOnly = true)
public EstadisticasResponse calcularEstadisticas(UUID establecimientoId, String periodoValor) {
    PeriodoDTO periodo = calcularPeriodo(periodoValor);

        KpisDTO kpis = new KpisDTO();
        calcularKpisOcupacion(establecimientoId, periodo, kpis);
        calcularKpisBeneficios(establecimientoId, periodo, kpis);
        calcularKpisCancelacion(establecimientoId, periodo, kpis);

        return new EstadisticasResponse(periodo, kpis, null, null);
    }

    /**
     * Calcula los KPIs de ocupación global porcetaje de cupos reservados sobre los cupos ofertados
     */
    private void calcularKpisOcupacion(UUID establecimientoId, PeriodoDTO periodo, KpisDTO kpis) {
        // Rango inclusivo en días: desde 00:00 hasta el final del día "hasta"
        LocalDateTime desde = periodo.getDesde().atStartOfDay();
        LocalDateTime hasta = periodo.getHasta().plusDays(1).atStartOfDay();

        int filled = (int) reservaRepository.contarCuposEnRango(establecimientoId, ESTADOS_CONFIRMADOS, desde, hasta);
        int total = (int) actividadRepository.sumarCuposOfertadosEnRango(establecimientoId, desde, hasta);
        int pct = total == 0 ? 0 : (int) Math.round(filled * 100.0 / total);

        kpis.setOcupacionFilled(filled);
        kpis.setOcupacionTotal(total);
        kpis.setOcupacionPct(pct);
    }

    /**
     * Calcula los beneficios del productor (suma de subTotalProductor de reservas confirmadas) en el rango actual
     * y su variación porcentual contra el rango anterior. El delta es null si el período anterior no tuvo beneficios.
     */
    private void calcularKpisBeneficios(UUID establecimientoId, PeriodoDTO periodo, KpisDTO kpis) {
        LocalDateTime desdeActual = periodo.getDesde().atStartOfDay();
        LocalDateTime hastaActual = periodo.getHasta().plusDays(1).atStartOfDay();
        LocalDateTime desdeAnterior = periodo.getDesdeAnterior().atStartOfDay();
        LocalDateTime hastaAnterior = periodo.getHastaAnterior().atStartOfDay();

        BigDecimal actual = Optional.ofNullable(
                reservaRepository.sumarSubTotalProductorEnRango(establecimientoId, ESTADOS_CONFIRMADOS, desdeActual, hastaActual)
        ).orElse(BigDecimal.ZERO);

        BigDecimal anterior = Optional.ofNullable(
                reservaRepository.sumarSubTotalProductorEnRango(establecimientoId, ESTADOS_CONFIRMADOS, desdeAnterior, hastaAnterior)
        ).orElse(BigDecimal.ZERO);

        Integer delta = (anterior.compareTo(BigDecimal.ZERO) == 0)
                ? null
                : actual.subtract(anterior)
                  .multiply(BigDecimal.valueOf(100))
                  .divide(anterior, 0, RoundingMode.HALF_UP)
                  .intValue();

        kpis.setBeneficios(actual);
        kpis.setBeneficiosDelta(delta);


        kpis.setBeneficios(actual);
        kpis.setBeneficiosDelta(delta);
    }

    /**
     * Calcula la cantidad de reservas canceladas en el rango actual y su proporción sobre el total de reservas
     * gestionadas (confirmadas + canceladas) en ese mismo rango. El porcentaje es 0 si no hubo reservas gestionadas.
     */
    private void calcularKpisCancelacion(UUID establecimientoId, PeriodoDTO periodo, KpisDTO kpis) {
        // Rango inclusivo en días: desde 00:00 hasta el final del día "hasta"
        LocalDateTime desde = periodo.getDesde().atStartOfDay();
        LocalDateTime hasta = periodo.getHasta().plusDays(1).atStartOfDay();

        int canceladas = (int) reservaRepository.contarReservasEnRango(establecimientoId, ESTADOS_CANCELADOS, desde, hasta);
        int gestionadas = (int) reservaRepository.contarReservasEnRango(establecimientoId, ESTADOS_GESTIONADOS, desde, hasta);
        int pct = gestionadas == 0 ? 0 : (int) Math.round(canceladas * 100.0 / gestionadas);

        kpis.setCancelacionCount(canceladas);
        kpis.setCancelacionPct(pct);
    }



    public PeriodoDTO calcularPeriodo(String periodoValor) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = switch (periodoValor) {
            case "30d" -> hoy.minusDays(30);
            case "6m"  -> hoy.minusMonths(6);
            case "12m" -> hoy.minusMonths(12);
            default -> throw new IllegalArgumentException("Período inválido: " + periodoValor);
        };
        long duracionDias = ChronoUnit.DAYS.between(desde, hoy);
        LocalDate hastaAnterior = desde;
        LocalDate desdeAnterior = desde.minusDays(duracionDias);

        return new PeriodoDTO(periodoValor, labelPara(periodoValor), desde, hoy, desdeAnterior, hastaAnterior);
    }
    private String labelPara(String periodoValor) {
        return switch (periodoValor) {
            case "30d" -> "Últimos 30 días";
            case "6m"  -> "Últimos 6 meses";
            case "12m" -> "Último año";
            default    -> "Período personalizado";
        };
    }

}
