package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.estadisticasproductor.ActividadPerformanceDTO;
import com.mza_agrotours.backend.dtos.estadisticasproductor.BarraDTO;
import com.mza_agrotours.backend.dtos.estadisticasproductor.EstadisticasResponse;
import com.mza_agrotours.backend.dtos.estadisticasproductor.KpisDTO;
import com.mza_agrotours.backend.dtos.estadisticasproductor.PeriodoDTO;
import com.mza_agrotours.backend.dtos.estadisticasproductor.SerieDTO;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
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
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

private static final Locale LOCALE_AR = new Locale("es", "AR");

@Transactional(readOnly = true)
public EstadisticasResponse calcularEstadisticas(UUID establecimientoId, String periodoValor) {
    PeriodoDTO periodo = calcularPeriodo(periodoValor);

        KpisDTO kpis = new KpisDTO();
        calcularKpisOcupacion(establecimientoId, periodo, kpis);
        calcularKpisBeneficios(establecimientoId, periodo, kpis);
        calcularKpisCancelacion(establecimientoId, periodo, kpis);

        SerieDTO serie = calcularSerie(establecimientoId, periodo);

        List<ActividadPerformanceDTO> actividades = calcularPerformanceActividades(establecimientoId, periodo);

        return new EstadisticasResponse(periodo, kpis, serie, actividades);
    }

    /**
     * Arma la tabla de performance por actividad para todas las actividades vigentes del establecimiento en el rango
     * actual.
     */
    private List<ActividadPerformanceDTO> calcularPerformanceActividades(UUID establecimientoId, PeriodoDTO periodo) {

        LocalDateTime desde = periodo.getDesde().atStartOfDay();
        LocalDateTime hasta = periodo.getHasta().plusDays(1).atStartOfDay();

        Map<UUID, Integer> cuposPorActividad = new HashMap<>();
        for (Object[] fila : actividadRepository.sumarCuposOfertadosEnRangoPorActividad(establecimientoId, desde, hasta)) {
            cuposPorActividad.put((UUID) fila[0], ((Number) fila[1]).intValue());
        }

        Map<UUID, Integer> reservasPorActividad = new HashMap<>();
        Map<UUID, BigDecimal> ingresosPorActividad = new HashMap<>();
        for (Object[] fila : reservaRepository.contarYSumarReservasEnRangoPorActividad(
                establecimientoId, ESTADOS_CONFIRMADOS, desde, hasta)) {
            UUID actividadId = (UUID) fila[0];
            reservasPorActividad.put(actividadId, ((Number) fila[1]).intValue());
            ingresosPorActividad.put(actividadId, Optional.ofNullable((BigDecimal) fila[2]).orElse(BigDecimal.ZERO));
        }

        List<ActividadPerformanceDTO> resultado = new ArrayList<>();
        for (Actividad actividad : actividadRepository.findVigentesConCultivosByEstablecimientoId(establecimientoId)) {
            UUID id = actividad.getId();
            int cupos = cuposPorActividad.getOrDefault(id, 0);
            int reservas = reservasPorActividad.getOrDefault(id, 0);
            int ocupacion = cupos == 0 ? 0 : (int) Math.round(reservas * 100.0 / cupos);
            String cultivo = actividad.getCultivos().stream()
                    .map(TipoCultivo::getNombre)
                    .collect(Collectors.joining(", "));

            resultado.add(new ActividadPerformanceDTO(id, actividad.getNombre(), cultivo, cupos, reservas, ocupacion,
                    ingresosPorActividad.getOrDefault(id, BigDecimal.ZERO)));
        }
        return resultado;

    }

    /**
     * Arma la serie del gráfico de barras: por cada sub-período (semana para "30d", mes calendario para "6m"/"12m")
     * cuenta las reservas confirmadas y suma su subTotalProductor. Los sub-períodos sin reservas se incluyen con 0
     * para que el eje sea continuo.
     */
    private SerieDTO calcularSerie(UUID establecimientoId, PeriodoDTO periodo) {
        LocalDateTime desde = periodo.getDesde().atStartOfDay();
        LocalDateTime hasta = periodo.getHasta().plusDays(1).atStartOfDay();

        List<LocalDateTime[]> intervalos = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        String title;
        String sub = periodo.getLabel();

        if ("30d".equals(periodo.getValor())) {
            title = "Reservas totales semanales";
            for (int i = 0; i < 4; i++) {
                LocalDateTime inicio = desde.plusWeeks(i);
                // La última semana se extiende hasta el final del rango para no dejar días afuera
                LocalDateTime fin = (i == 3) ? hasta : inicio.plusWeeks(1);
                intervalos.add(new LocalDateTime[]{inicio, fin});
                labels.add("Sem " + (i + 1));
            }
        } else {
            title = "Reservas totales mensuales";
            YearMonth mesFin = YearMonth.from(periodo.getHasta());
            for (YearMonth mes = YearMonth.from(periodo.getDesde()); !mes.isAfter(mesFin); mes = mes.plusMonths(1)) {
                // Los meses de los extremos se recortan al rango del período
                LocalDateTime inicio = max(mes.atDay(1).atStartOfDay(), desde);
                LocalDateTime fin = min(mes.plusMonths(1).atDay(1).atStartOfDay(), hasta);
                intervalos.add(new LocalDateTime[]{inicio, fin});
                labels.add(abreviaturaMes(mes.getMonth()));
            }
        }

        int[] cantidades = new int[intervalos.size()];
        BigDecimal[] ganancias = new BigDecimal[intervalos.size()];
        Arrays.fill(ganancias, BigDecimal.ZERO);

        List<Object[]> reservas = reservaRepository.findFechaYSubTotalProductorEnRango(
                establecimientoId, ESTADOS_CONFIRMADOS, desde, hasta);

        for (Object[] fila : reservas) {
            LocalDateTime fecha = (LocalDateTime) fila[0];
            BigDecimal subTotal = Optional.ofNullable((BigDecimal) fila[1]).orElse(BigDecimal.ZERO);
            for (int i = 0; i < intervalos.size(); i++) {
                if (!fecha.isBefore(intervalos.get(i)[0]) && fecha.isBefore(intervalos.get(i)[1])) {
                    cantidades[i]++;
                    ganancias[i] = ganancias[i].add(subTotal);
                    break;
                }
            }
        }

        List<BarraDTO> barras = new ArrayList<>();
        for (int i = 0; i < intervalos.size(); i++) {
            barras.add(new BarraDTO(labels.get(i), cantidades[i], ganancias[i]));
        }

        return new SerieDTO(title, sub, barras);
    }

    // "Ene", "Feb", ..., "Sep", "Dic": se normaliza porque el JDK puede devolver "sept." o minúsculas
    private String abreviaturaMes(Month mes) {
        String nombre = mes.getDisplayName(TextStyle.SHORT, LOCALE_AR).replace(".", "");
        return nombre.substring(0, 1).toUpperCase(LOCALE_AR) + nombre.substring(1, 3);
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
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
