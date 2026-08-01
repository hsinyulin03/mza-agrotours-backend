package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.EstablecimientoPorActividad;
import com.mza_agrotours.backend.dtos.reservas.ListarReservaDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.EstablecimientoNotFoundException;
import com.mza_agrotours.backend.exceptions.UsuarioDeactivatedException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.exceptions.reservas.ReservaNotFoundException;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    /**
     * Devuelve todos los datos necesarios para que un visitante consulte una reserva específica<p></p>
     *
     * @param id UUID de la Reserva
     * @param firebaseUID UID en firebase
     * @return <code>ConsultarReservaDTO</code> con los datos de una reserva
     * @throws ReservaNotFoundException si la reserva no existe o no pertenece al usuario autenticado
     * @throws UsuarioDeactivatedException si el usuario dueño de la reserva está dado de baja
     */
    @Transactional
    public ConsultarReservaDTO getConsultarReserva(UUID id, String firebaseUID){
        // Obtenemos la reserva, si no existe error.
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(ReservaNotFoundException::new);

        Establecimiento establecimiento = establecimientoRepository.findEstablecimientoByActividadId(reserva.getActividad().getId())
                .orElseThrow(EstablecimientoNotFoundException::new);

        // Verificar que la reserva sea del usuario. Si no lo es, NOT FOUND para evitar dar información a no autorizados
         if (!reserva.getVisitante().getUsuario().getFirebaseUID().equals(firebaseUID))
             throw new ReservaNotFoundException();

         // Verificar que el usuario esté de alta
        if (reserva.getVisitante().getUsuario().getFechaHoraBaja() != null)
            throw new UsuarioDeactivatedException();

        // Armamos el DTO
        return reservaMapper.reservaToConsultarReservaDTO(reserva, establecimiento);
    }

    @Transactional(readOnly = true)
    public List<ListarReservaDTO> getListarReservas(String emailUsuario){

        List<ListarReservaDTO> dtos = new ArrayList<>();

        // Gettear al usuario y visitante
        Usuario usuario = usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        Visitante visitante = visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);

        // Obtenemos las reservas. Si está vacío devolvemos el array vacío
        List<Reserva> reservas = reservaRepository.findByVisitanteId(visitante.getId());
        if (reservas.isEmpty())
            return dtos;

        // Obtenemos las id de las actividades que ha reservado
        List<UUID> uuidsActividad = new ArrayList<>();
        reservas.forEach(reserva -> uuidsActividad.add(reserva.getActividad().getId()));

        // Buscamos los establecimientos de las actividades en masse
        List<EstablecimientoPorActividad> establecimientosPorActividad = establecimientoRepository.findEstablecimientosByActividadIds(uuidsActividad);

        Map<UUID, Establecimiento> actividadEstablecimientoMap = new HashMap<>();
        establecimientosPorActividad.forEach(establecimientoPorActividad ->
                actividadEstablecimientoMap.put(establecimientoPorActividad.actividadID(), establecimientoPorActividad.establecimiento())
        );

        // Armamos el dto para cada reserva
        for (Reserva reserva: reservas){
            Establecimiento establecimiento =  Optional.ofNullable(actividadEstablecimientoMap.get(reserva.getActividad().getId()))
                    .orElseThrow(EstablecimientoNotFoundException::new);

            dtos.add(reservaMapper.reservaToListarReservaDTO(reserva, establecimiento));
        }

        // Armamos el DTO
        return dtos;
    }
}
