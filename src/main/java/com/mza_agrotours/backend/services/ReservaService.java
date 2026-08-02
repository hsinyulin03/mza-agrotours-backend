package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.EstablecimientoNotFoundException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.exceptions.reservas.ReservaNotFoundException;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VisitanteRepository visitanteRepository;

    public ReservaService(ReservaRepository reservaRepository, ReservaMapper reservaMapper, EstablecimientoRepository establecimientoRepository, UsuarioRepository usuarioRepository, VisitanteRepository visitanteRepository) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.visitanteRepository = visitanteRepository;
    }

    @Transactional
    public ConsultarReservaDTO getConsultarReserva(UUID id, String emailUsuario){

        // Gettear al usuario y visitante
        Usuario usuario = usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        Visitante visitante = visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);

        // Obtenemos la reserva, si no existe error.
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(ReservaNotFoundException::new);

        // Verificar que la reserva sea del usuario. Si no lo es, NOT FOUND para evitar dar información a no autorizados
         if (!reserva.getVisitante().getId().equals(visitante.getId()))
             throw new ReservaNotFoundException();

        // Buscar el establecimiento de esa actividad
        Establecimiento establecimiento = establecimientoRepository.findEstablecimientoByActividadId(reserva.getActividad().getId())
                .orElseThrow(EstablecimientoNotFoundException::new);

        // Armamos el DTO
        return reservaMapper.reservaToConsultarReservaDTO(reserva, establecimiento);
    }
}
