package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDetalleDTO;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.Foto;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:ss");

    public ConsultarReservaDTO getConsultarReserva(UUID id){
        // Iniciamos los DTO
        ConsultarReservaDTO dtoReserva;
        List<ConsultarReservaDetalleDTO> dtoReservaDetalles = new ArrayList<>();
        List<Pair<String, String>> pairFotos = new ArrayList<>();

        // Obtenemos las entidades que vamos a usar repetidamente
        Reserva reserva = reservaRepository.getReferenceById(id);
        List<ReservaDetalle> reservaDetalles = reserva.getReservaDetalles();
        Actividad actividad = reserva.getActividad();
        ActividadDia actividadDia = reserva.getActividadDia();
        Establecimiento establecimiento = actividad.getEstablecimiento();
        List<Foto> fotos = actividad.getFotos();

        // Poblamos dtoReservaDetalles
        for (ReservaDetalle reservaDetalle : reservaDetalles){
            LocalDate fechaNacimiento = reservaDetalle.getFechaNacimiento();
            LocalDate hoy = LocalDate.now();
            String rangoEtario; // TODO Hay que ver cómo hacer el tema de los rangoEtarios parametrizables
            if (fechaNacimiento.plusYears(18).isBefore(hoy)) {
                rangoEtario = "Adulto";
            } else {
                rangoEtario = "Otro";
            }
            ConsultarReservaDetalleDTO rdDto = new ConsultarReservaDetalleDTO(
                    reservaDetalle.getRenglon(),
                    reservaDetalle.getNombre(),
                    rangoEtario,
                    reservaDetalle.getSubtotal()
            );
            dtoReservaDetalles.add(rdDto);
        }

        // Poblamos fotos
        for (Foto foto : fotos){
            Pair<String, String> pairFoto = Pair.of(foto.getUrl(), foto.getNombre());
            pairFotos.add(pairFoto);
        }

        // Armamos el DTO final
        dtoReserva = new ConsultarReservaDTO(
                reserva.getTotalReserva(),
                reserva.getId(),
                reserva.getEstadoActual().getEstadoReserva().getEstado(),
                dtoReservaDetalles.size(),
                dtoReservaDetalles,
                actividadDia.getFechaHoraInicio().format(dateFormat),
                actividadDia.getFechaHoraInicio().format(timeFormat),
                actividadDia.getFechaHoraFin().format(timeFormat),
                actividad.getNombre(),
                "http://agroturros.com/" +  establecimiento.getId() + "/" + actividad.getId(),
                establecimiento.getUbicacion(),
                establecimiento.getRazonSocial(),
                "http://agroturros.com/" +  establecimiento.getId(),
                pairFotos
                );
        return dtoReserva;
    }
}
