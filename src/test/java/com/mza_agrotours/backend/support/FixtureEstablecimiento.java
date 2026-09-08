package com.mza_agrotours.backend.support;

import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FixtureEstablecimiento {

    @Autowired private FixtureCatalogo catalogo;
    @Autowired private FixtureProductor fixtureProductor;
    @Autowired private EstablecimientoRepository establecimientoRepository;

    public Establecimiento establecimientoActivo() {
        return establecimientoEn(EstadoEstablecimientoNombre.ACTIVO);
    }

    public Establecimiento establecimientoEn(EstadoEstablecimientoNombre estadoNombre) {
        int n = Seq.next();

        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setNombre("Finca " + n);
        establecimiento.setRazonSocial("Finca " + n + " S.A.");
        establecimiento.setCuit(String.format("20%09d", n));
        establecimiento.setFechaHoraAlta(LocalDateTime.now());
        establecimiento.setDescripcion("Establecimiento de prueba " + n);
        establecimiento.setTelefono("2610000000");
        establecimiento.setEmail("finca" + n + "@test.local");
        establecimiento.setUbicacion("-32.8895,-68.8458");
        establecimiento.setCvu(String.format("%022d", n));
        establecimiento.setDepartamento(catalogo.unDepartamento());
        establecimiento = this.establecimientoRepository.save(establecimiento);

        fixtureProductor.titularDe(establecimiento);
        establecimiento.cambiarEstado(catalogo.estadoEstablecimiento(estadoNombre), "Alta de prueba");
        return this.establecimientoRepository.save(establecimiento);
    }
}
