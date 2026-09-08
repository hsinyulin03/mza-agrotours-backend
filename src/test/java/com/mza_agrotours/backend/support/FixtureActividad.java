package com.mza_agrotours.backend.support;

import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadDiaEstado;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FixtureActividad {

    @Autowired private FixtureCatalogo catalogo;
    @Autowired private ActividadRepository actividadRepository;

    public Actividad actividadPublicadaEn(Establecimiento establecimiento) {
        int n = Seq.next();

        Actividad actividad = new Actividad();
        actividad.setNombre("Actividad " + n);
        actividad.setDescripcion("Actividad de prueba numero " + n);
        actividad.setCuposMax(20);
        actividad.setEstado(catalogo.estadoActividad(EstadoActividadNombre.PUBLICADO));
        actividad.setEstablecimiento(establecimiento);
        return this.actividadRepository.save(actividad);
    }

    public Actividad actividadConDiaReservableEn(Establecimiento establecimiento) {
        Actividad actividad = actividadPublicadaEn(establecimiento);

        ActividadDiaEstado estado = new ActividadDiaEstado();
        estado.setFechaHoraInicio(LocalDateTime.now().minusDays(1));
        estado.setEstado(catalogo.estadoActividadDia(EstadoActividadDiaNombre.ACTIVA));

        ActividadDia dia = new ActividadDia();
        // handleIniciarReserva solo mira dias cuya fechaHoraInicio ya paso.
        dia.setFechaHoraInicio(LocalDateTime.now().minusHours(1));
        dia.setFechaHoraFin(LocalDateTime.now().plusHours(3));
        dia.setCuposMax(20);
        dia.registrarNuevoEstado(estado);

        actividad.addActividadDia(dia);
        return this.actividadRepository.save(actividad);
    }
}
