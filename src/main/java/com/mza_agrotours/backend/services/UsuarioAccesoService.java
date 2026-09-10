package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.acceso.AccesoDTO;
import com.mza_agrotours.backend.dtos.acceso.AccesoSuspensionDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.entities.productor.ProductorEstado;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import com.mza_agrotours.backend.mappers.AccesoMapper;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ProductorRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioAccesoService {
    private final RolRepository rolRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final AccesoMapper accesoMapper;
    private final ProductorRepository productorRepository;

    public UsuarioAccesoService(RolRepository rolRepository,
                                EstablecimientoRepository establecimientoRepository,
                                AdministradorSistemasRepository administradorSistemasRepository,
                                AccesoMapper accesoMapper,
                                ProductorRepository productorRepository) {
        this.rolRepository = rolRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.accesoMapper = accesoMapper;
        this.productorRepository = productorRepository;
    }

    public List<AccesoDTO> obtenerAccesosUsuario(Usuario usuario) {
        List<AccesoDTO> accesos = new ArrayList<>();

        AccesoDTO accesoAdministrador = obtenerAccesosAdministrador(usuario);
        if (accesoAdministrador != null) {
            accesos.add(accesoAdministrador);
        }

        List<AccesoDTO> accesosProductor = obtenerAccesosProductor(usuario);
        accesos.addAll(accesosProductor);

        return accesos;
    }

    /**
     * Obtiene los accesos del administrador si es administrador vigente.
     * En caso contrario retorna null.
     * @param usuario
     * @return AccesoDTO con los accesos del administrador o null si no es administrador vigente.
     */
    private AccesoDTO obtenerAccesosAdministrador(Usuario usuario) {
        AdministradorSistemas administradorSistemas = administradorSistemasRepository
                .findByUsuarioAndFechaHoraBajaIsNull(usuario).orElse(null);

        if (administradorSistemas == null) {
            return null;
        }

        return this.accesoMapper
                .rolToAccesoDTO(administradorSistemas.getRol());
    }

    private List<AccesoDTO> obtenerAccesosProductor(Usuario usuario) {
        List<Productor> productores = this.productorRepository
                .findByUsuarioAndFechaHoraBajaIsNull(usuario);

        List<AccesoDTO> accesos = new ArrayList<>();
        for (Productor productor : productores) {
            AccesoDTO accesoDTO = this.accesoMapper.rolToAccesoDTO(productor.getRol());

            ProductorEstado estadoActual = obtenerEstadoActual(productor);

            if (!estadoActual.getEstadoProductor().getNombre()
                    .equals(EstadoProductorNombre.LICENCIA)) {
                accesos.add(accesoDTO);
                continue;
            }


            AccesoSuspensionDTO accesoSuspensionDTO = new AccesoSuspensionDTO();
            accesoSuspensionDTO.setMotivo(estadoActual.getMotivo());
            accesoSuspensionDTO.setFechaHoraFin(estadoActual.getFechaHoraFinPrevista());

            accesoDTO.setSuspension(accesoSuspensionDTO);

            accesos.add(accesoDTO);
        }


        return accesos;
    }

    private ProductorEstado obtenerEstadoActual(Productor productor) {
        return this.productorRepository.findProductorEstadoActualByProductorId(productor.getId())
                .orElseThrow(() -> new IllegalStateException("El producto no tiene estado actual"));
    }
}
