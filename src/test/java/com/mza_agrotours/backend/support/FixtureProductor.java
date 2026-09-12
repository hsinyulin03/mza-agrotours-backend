package com.mza_agrotours.backend.support;

import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FixtureProductor {

    @Autowired private FixtureCatalogo catalogo;
    @Autowired private FixtureUsuario fixtureUsuario;
    @Autowired private ProductorRepository productorRepository;
    @Autowired private EstablecimientoRepository establecimientoRepository;

    /** Un productor mas del establecimiento, activo y sin ser titular. */
    public Productor productorDe(Establecimiento establecimiento) {
        return productorDeEn(establecimiento, EstadoProductorNombre.ACTIVO);
    }

    public Productor productorDeEn(Establecimiento establecimiento, EstadoProductorNombre estadoNombre) {
        Productor productor = new Productor();
        productor.setFechaHoraAlta(LocalDateTime.now());
        productor.setEstablecimiento(establecimiento);
        productor.setUsuario(fixtureUsuario.usuario("productor"));
        productor.setRol(catalogo.rolProductor());
        productor.cambiarEstado(catalogo.estadoProductor(estadoNombre),
                "Alta de prueba", LocalDateTime.now(), null);
        return this.productorRepository.save(productor);
    }

    /**
     * El titular, con la referencia de vuelta ya persistida. Productor.establecimiento es
     * optional=false y Establecimiento.titular apunta de vuelta: el ciclo solo se puede cerrar
     * en una segunda escritura, con el establecimiento ya guardado.
     */
    public Productor titularDe(Establecimiento establecimiento) {
        Productor titular = productorDe(establecimiento);
        establecimiento.setTitular(titular);
        this.establecimientoRepository.save(establecimiento);
        return titular;
    }
}
