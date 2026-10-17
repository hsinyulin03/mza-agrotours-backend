package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.deudas.CambiarEstadoDeudaDTO;
import com.mza_agrotours.backend.dtos.deudas.PagoDeudaDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimiento;
import com.mza_agrotours.backend.entities.deudas.DeudaPago;
import com.mza_agrotours.backend.entities.deudas.EstadoDeudaEstablecimiento;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.enums.*;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.DeudaError;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.DeudaPagoRepository;
import com.mza_agrotours.backend.repositories.DeudaRepository;
import com.mza_agrotours.backend.repositories.EstadoDeudaEstablecimientoRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Cambio de estado de una deuda de un establecimiento por parte de un administrador (US-RESE-12).
 */
class DeudaEstablecimientoServiceCambioEstadoTest {

    private static final String EMAIL_ADMIN = "admin@test.com";

    private DeudaRepository deudaRepository;
    private DeudaPagoRepository deudaPagoRepository;
    private NotificacionService notificacionService;
    private DeudaEstablecimientoService service;

    private final AdministradorSistemas administrador = new AdministradorSistemas();
    private final Establecimiento establecimiento = new Establecimiento();

    @BeforeEach
    void setUp() {
        deudaRepository = mock(DeudaRepository.class);
        deudaPagoRepository = mock(DeudaPagoRepository.class);
        notificacionService = mock(NotificacionService.class);
        establecimiento.setId(UUID.randomUUID());
        establecimiento.setNombre("Finca");

        AdministradorSistemasRepository administradorSistemasRepository = mock(AdministradorSistemasRepository.class);
        when(administradorSistemasRepository.findByEmailActivo(EMAIL_ADMIN)).thenReturn(Optional.of(administrador));

        EstadoDeudaEstablecimientoRepository estadoDeudaRepository = mock(EstadoDeudaEstablecimientoRepository.class);
        when(estadoDeudaRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoDeudaEstablecimiento(LocalDateTime.now(), null, inv.<EstadoDeudaEstablecimientoNombre>getArgument(0))));
        EstadoPagoRepository estadoPagoRepository = mock(EstadoPagoRepository.class);
        when(estadoPagoRepository.findByNombre(any())).thenAnswer(inv ->
                Optional.of(new EstadoPago(inv.<EstadoPagoNombre>getArgument(0), LocalDateTime.now(), null)));

        service = new DeudaEstablecimientoService(deudaRepository, deudaPagoRepository, estadoDeudaRepository,
                estadoPagoRepository, administradorSistemasRepository, notificacionService);
    }

    // ---------- Datos ----------

    /** Deuda de $10000 en el estado indicado, con lo ya devuelto. */
    private DeudaEstablecimiento deuda(EstadoDeudaEstablecimientoNombre estado, String devuelto) {
        DeudaEstablecimiento deuda = new DeudaEstablecimiento(LocalDateTime.now().minusDays(5), new BigDecimal("10000.00"),
                BigDecimal.ZERO, null, new ArrayList<>(), null, establecimiento);
        deuda.setId(UUID.randomUUID());
        deuda.cambiarEstado(new EstadoDeudaEstablecimiento(null, null, estado), LocalDateTime.now().minusDays(5),
                new BigDecimal(devuelto), "inicial");
        when(deudaRepository.findByIdForUpdate(deuda.getId())).thenReturn(Optional.of(deuda));
        when(deudaRepository.findDetalleById(deuda.getId())).thenReturn(Optional.of(deuda));
        return deuda;
    }

    private static PagoDeudaDTO pago(String monto) {
        return new PagoDeudaDTO(LocalDateTime.now().minusHours(2).withNano(0), new BigDecimal(monto), "Transferencia", "TRX-1");
    }

    private static CambiarEstadoDeudaDTO devolucion(PagoDeudaDTO pago) {
        return new CambiarEstadoDeudaDTO(TipoCambioEstadoDeuda.DEVOLUCION, "Devolución", pago);
    }

    private static CambiarEstadoDeudaDTO acuerdo(PagoDeudaDTO pago) {
        return new CambiarEstadoDeudaDTO(TipoCambioEstadoDeuda.ACUERDO_MEDIO, "Acuerdo", pago);
    }

    private static EstadoDeudaEstablecimientoNombre estadoActual(DeudaEstablecimiento deuda) {
        return deuda.getEstadoActual().getEstadoDeudaEstablecimiento().getNombre();
    }

    private DeudaPago pagoGuardado() {
        ArgumentCaptor<DeudaPago> captor = ArgumentCaptor.forClass(DeudaPago.class);
        verify(deudaPagoRepository).save(captor.capture());
        return captor.getValue();
    }

    private void verificarNotificacion(TipoNotificacionNombre tipo, Object... datos) {
        verify(notificacionService).notificarProductores(eq(establecimiento), eq(PermisoCodigo.GESTIONAR_REEMBOLSOS_PENDIENTES),
                eq(tipo), eq("/panel/" + establecimiento.getId()), eq(datos));
    }

    // ---------- Casos ----------

    @Test
    void unaDevolucionMenorAlRestanteLaDejaEnDevolucionParcial() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, "0");
        PagoDeudaDTO pago = pago("4000.00");

        service.cambiarEstadoDeuda(deuda.getId(), devolucion(pago), EMAIL_ADMIN);

        assertEquals(EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL, estadoActual(deuda));
        assertEquals(new BigDecimal("4000.00"), deuda.getMontoTotalDevuelto());
        assertEquals(new BigDecimal("4000.00"), deuda.getEstadoActual().getMontoDevueltoTransaccion());
        assertEquals("Devolución", deuda.getEstadoActual().getMotivo());

        DeudaPago deudaPago = pagoGuardado();
        assertSame(deuda.getEstadoActual(), deudaPago.getDeudaEstablecimientoEstado());
        assertSame(administrador, deudaPago.getCargadoPorAdmin());
        assertEquals("Transferencia", deudaPago.getMetodoPagoReal());
        assertNotNull(deudaPago.getFechaHoraCarga());

        Pago p = deudaPago.getPago();
        assertEquals(MetodoPago.MANUAL, p.getMetodoPago());
        assertEquals(EstadoPagoNombre.APROBADO, p.getEstadoActual().getEstadoPago().getNombre());
        assertEquals(new BigDecimal("4000.00"), p.getMontoTotal());
        assertEquals("TRX-1", p.getIdTransaccionExterna());
        assertEquals(pago.fechaHoraPago(), p.getFechaHoraPago());

        verify(deudaRepository).flush();
        verify(deudaRepository, never()).save(any());
        verificarNotificacion(TipoNotificacionNombre.DEUDA_DEVOLUCION_PARCIAL_PRODUCTOR, "4000.00", "10000.00", "6000.00");
    }

    @Test
    void unaDevolucionQueCompletaLaDeudaLaDejaEnDevolucionTotal() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL, "4000.00");

        service.cambiarEstadoDeuda(deuda.getId(), devolucion(pago("6000")), EMAIL_ADMIN);

        assertEquals(EstadoDeudaEstablecimientoNombre.DEVOLUCION_TOTAL, estadoActual(deuda));
        assertEquals(0, deuda.getMontoTotalDevuelto().compareTo(deuda.getMontoDeuda()));
        verify(deudaPagoRepository).save(any());
        verificarNotificacion(TipoNotificacionNombre.DEUDA_DEVOLUCION_TOTAL_PRODUCTOR, "6000", "10000.00");
    }

    @Test
    void unaDevolucionMayorAlRestanteSeRechaza() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL, "4000.00");

        AppException ex = assertThrows(AppException.class, () ->
                service.cambiarEstadoDeuda(deuda.getId(), devolucion(pago("6000.01")), EMAIL_ADMIN));

        assertEquals(DeudaError.MONTO_INVALIDO, ex.getErrorCode());
        assertEquals(EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL, estadoActual(deuda));
        verify(deudaPagoRepository, never()).save(any());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void unaDevolucionSinPagoSeRechaza() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, "0");

        AppException ex = assertThrows(AppException.class, () ->
                service.cambiarEstadoDeuda(deuda.getId(), devolucion(null), EMAIL_ADMIN));

        assertEquals(DeudaError.PAGO_OBLIGATORIO, ex.getErrorCode());
        assertEquals(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, estadoActual(deuda));
    }

    @Test
    void unAcuerdoSinPagoCierraLaDeudaSinGuardarPago() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL, "4000.00");

        service.cambiarEstadoDeuda(deuda.getId(), acuerdo(null), EMAIL_ADMIN);

        assertEquals(EstadoDeudaEstablecimientoNombre.ACUERDO_MEDIO, estadoActual(deuda));
        assertEquals(new BigDecimal("4000.00"), deuda.getMontoTotalDevuelto());
        assertEquals(0, deuda.getEstadoActual().getMontoDevueltoTransaccion().signum());
        assertEquals("Acuerdo", deuda.getEstadoActual().getMotivo());
        verify(deudaPagoRepository, never()).save(any());
        verificarNotificacion(TipoNotificacionNombre.DEUDA_ACUERDO_MEDIO_PRODUCTOR, "10000.00", "4000.00");
    }

    @Test
    void unAcuerdoConPagoLoGuardaYSumaLoDevuelto() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, "0");

        service.cambiarEstadoDeuda(deuda.getId(), acuerdo(pago("7000.00")), EMAIL_ADMIN);

        assertEquals(EstadoDeudaEstablecimientoNombre.ACUERDO_MEDIO, estadoActual(deuda));
        assertEquals(new BigDecimal("7000.00"), deuda.getMontoTotalDevuelto());
        assertSame(deuda.getEstadoActual(), pagoGuardado().getDeudaEstablecimientoEstado());
        verificarNotificacion(TipoNotificacionNombre.DEUDA_ACUERDO_MEDIO_PRODUCTOR, "10000.00", "7000.00");
    }

    @Test
    void unAcuerdoConPagoPorTodoLoRestanteSePermite() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, "0");

        service.cambiarEstadoDeuda(deuda.getId(), acuerdo(pago("10000.00")), EMAIL_ADMIN);

        assertEquals(EstadoDeudaEstablecimientoNombre.ACUERDO_MEDIO, estadoActual(deuda));
        verify(deudaPagoRepository).save(any());
    }

    @Test
    void unAcuerdoConPagoMayorAlRestanteSeRechaza() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, "0");

        AppException ex = assertThrows(AppException.class, () ->
                service.cambiarEstadoDeuda(deuda.getId(), acuerdo(pago("10000.01")), EMAIL_ADMIN));

        assertEquals(DeudaError.MONTO_INVALIDO, ex.getErrorCode());
    }

    @Test
    void unPagoConUnIdExternoYaCargadoEnLaDeudaSeRechaza() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL, "4000.00");
        when(deudaPagoRepository.existsByDeudaIdAndIdPagoExterno(deuda.getId(), "TRX-1")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () ->
                service.cambiarEstadoDeuda(deuda.getId(), devolucion(pago("1000")), EMAIL_ADMIN));

        assertEquals(DeudaError.PAGO_DUPLICADO, ex.getErrorCode());
        assertEquals(EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL, estadoActual(deuda));
        assertEquals(new BigDecimal("4000.00"), deuda.getMontoTotalDevuelto());
        verify(deudaPagoRepository, never()).save(any());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void unAcuerdoSinPagoNoBuscaDuplicados() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, "0");

        service.cambiarEstadoDeuda(deuda.getId(), acuerdo(null), EMAIL_ADMIN);

        verify(deudaPagoRepository, never()).existsByDeudaIdAndIdPagoExterno(any(), any());
    }

    @Test
    void unaDeudaFinalizadaNoCambiaDeEstado() {
        for (EstadoDeudaEstablecimientoNombre estado : new EstadoDeudaEstablecimientoNombre[]{
                EstadoDeudaEstablecimientoNombre.DEVOLUCION_TOTAL, EstadoDeudaEstablecimientoNombre.ACUERDO_MEDIO}) {
            DeudaEstablecimiento deuda = deuda(estado, "0");

            AppException ex = assertThrows(AppException.class, () ->
                    service.cambiarEstadoDeuda(deuda.getId(), acuerdo(null), EMAIL_ADMIN));

            assertEquals(DeudaError.DEUDA_FINALIZADA, ex.getErrorCode());
            assertEquals(estado, estadoActual(deuda));
        }
        verify(deudaPagoRepository, never()).save(any());
        verifyNoInteractions(notificacionService);
    }

    @Test
    void unAdministradorInexistenteNoPuedeCambiarElEstado() {
        DeudaEstablecimiento deuda = deuda(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, "0");

        assertThrows(AppException.class, () ->
                service.cambiarEstadoDeuda(deuda.getId(), devolucion(pago("100")), "otro@test.com"));

        assertEquals(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, estadoActual(deuda));
    }
}
