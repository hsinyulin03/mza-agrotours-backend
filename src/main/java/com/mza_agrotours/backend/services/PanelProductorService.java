package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.PanelProductor.DTOResumenPanelProductor;
import com.mza_agrotours.backend.dtos.PanelProductor.GrupoDiaReservasDTO;
import com.mza_agrotours.backend.dtos.PanelProductor.PendientesDTO;
import com.mza_agrotours.backend.dtos.PanelProductor.ReseniasDTO;
import com.mza_agrotours.backend.dtos.PanelProductor.ReservaProximaDTO;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.repositories.CalificacionRepository;
import com.mza_agrotours.backend.repositories.CuentaMercadoPagoRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PanelProductorService {
    private static final int DIAS_PROXIMAS_RESERVAS = 3;
    private static final int CANTIDAD_ULTIMAS_RESENIAS = 3;

    @Autowired
    private ReembolsoRepository reembolsos;
    @Autowired
    private ActividadRepository actividades;
    @Autowired
    private CuentaMercadoPagoRepository cuentasMercadoPago;
    @Autowired
    private ReservaRepository reservas;
    @Autowired
    private CalificacionRepository calificaciones;

    @Transactional(readOnly = true)
    public DTOResumenPanelProductor obtenerResumen(UUID establecimientoId) {
        return new DTOResumenPanelProductor(
                obtenerPendientes(establecimientoId),
                obtenerProximasReservas(establecimientoId),
                obtenerResenias(establecimientoId));
    }

    private PendientesDTO obtenerPendientes(UUID establecimientoId) {
        Optional<CuentaMercadoPago> cuenta = cuentasMercadoPago.findFirstByEstablecimiento_IdAndFechaHoraBajaIsNull(establecimientoId);
        return new PendientesDTO(
                reembolsos.countPedidosByEstablecimiento(establecimientoId),
                reembolsos.findPedidoMasAntiguoByEstablecimiento(establecimientoId),
                actividades.countByEstablecimiento_IdAndEstado_NombreAndFechaHoraBajaIsNull(establecimientoId, EstadoActividadNombre.BORRADOR),
                cuenta.isPresent(),
                cuenta.map(CuentaMercadoPago::getFechaHoraExpiracionToken).orElse(null));
    }

    private List<GrupoDiaReservasDTO> obtenerProximasReservas(UUID establecimientoId) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hasta = ahora.toLocalDate().plusDays(DIAS_PROXIMAS_RESERVAS).atStartOfDay();
        List<ReservaProximaDTO> proximas = reservas.findProximasDelEstablecimiento(
                establecimientoId, List.of(EstadoReservaNombre.PAGADA), ahora, hasta);

        Map<LocalDate, List<ReservaProximaDTO>> porDia = proximas.stream()
                .collect(Collectors.groupingBy(r -> r.getFechaHoraInicio().toLocalDate(), TreeMap::new, Collectors.toList()));
        return porDia.entrySet().stream()
                .map(e -> new GrupoDiaReservasDTO(e.getKey(), e.getValue()))
                .toList();
    }

    private ReseniasDTO obtenerResenias(UUID establecimientoId) {
        return new ReseniasDTO(
                calificaciones.promedioPuntajeByEstablecimiento(establecimientoId),
                calificaciones.countByEstablecimiento(establecimientoId),
                calificaciones.findUltimasByEstablecimiento(establecimientoId, PageRequest.of(0, CANTIDAD_ULTIMAS_RESENIAS)));
    }
}
