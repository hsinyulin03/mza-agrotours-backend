package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VisitanteService {
    private final VisitanteRepository visitanteRepository;
    private final ReservaRepository reservaRepository;

    public VisitanteService(VisitanteRepository visitanteRepository, ReservaRepository reservaRepository) {
        this.visitanteRepository = visitanteRepository;
        this.reservaRepository = reservaRepository;
    }

    public boolean tieneReservasActivasByUsuario(Usuario usuario) {
        Optional<Visitante> optionalVisitante = this.visitanteRepository.findByUsuario(usuario);

        return optionalVisitante.filter(visitante -> this.reservaRepository
                .tieneReservasEnEstadoByVisitanteId(visitante.getId(), EstadoReservaNombre.PENDIENTE)).isPresent();
    }
}
