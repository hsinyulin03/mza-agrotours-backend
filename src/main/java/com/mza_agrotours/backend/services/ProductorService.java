package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.productor.EstadoProductor;
import com.mza_agrotours.backend.dtos.productor.Productor;
import com.mza_agrotours.backend.dtos.productor.ProductorEstado;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.enums.RolProtegido;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.ProductorError;
import com.mza_agrotours.backend.repositories.EstadoProductorRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductorService {
    private final EstadoProductorRepository estadoProductorRepository;
    private final RolRepository rolRepository;
    private final ProductorRepository productorRepository;

    public ProductorService(EstadoProductorRepository estadoProductorRepository,
                            RolRepository rolRepository,
                            ProductorRepository productorRepository) {
        this.estadoProductorRepository = estadoProductorRepository;
        this.rolRepository = rolRepository;
        this.productorRepository = productorRepository;
    }

    @Transactional
    public Productor crearProductorLider(Usuario usuarioProductor,
                                         Establecimiento establecimiento) {
        EstadoProductor estadoActivo = obtenerEstadoProductorByNombre(EstadoProductorNombre.ACTIVO);

        ProductorEstado productorEstado = new ProductorEstado();
        productorEstado.setFechaHoraInicio(LocalDateTime.now());
        productorEstado.setFechaHoraFin(null);
        productorEstado.setMotivo("Creacion productor lider");
        productorEstado.setEstadoProductor(estadoActivo);

        Productor productorLider = new Productor();
        productorLider.setFechaHoraAlta(LocalDateTime.now());
        productorLider.setEstablecimiento(establecimiento);
        productorLider.setUsuario(usuarioProductor);
        productorLider.setRol(obtenerRolProductorByNombre(RolProtegido.PRODUCTOR_LIDER.getNombre()));
        productorLider.getEstados().add(productorEstado);
        productorLider.setEstadoActual(estadoActivo);

        return this.productorRepository.save(productorLider);
    }

    private EstadoProductor obtenerEstadoProductorByNombre(EstadoProductorNombre estadoNombre) {
        return this.estadoProductorRepository
                .findByNombreAndFechaHoraBajaIsNull(estadoNombre)
                .orElseThrow(() -> new AppException(ProductorError.ESTADO_NO_CONFIGURADO));
    }

    private Rol obtenerRolProductorByNombre(String nombre) {
        return this.rolRepository
                .findByNombreAndFechaHoraBajaIsNull(nombre)
                .orElseThrow(() -> new AppException(ProductorError.ROL_NO_CONFIGURADO));
    }
}