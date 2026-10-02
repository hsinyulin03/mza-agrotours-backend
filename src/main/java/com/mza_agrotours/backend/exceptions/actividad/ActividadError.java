package com.mza_agrotours.backend.exceptions.actividad;

import com.mza_agrotours.backend.exceptions.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum ActividadError implements ErrorCode {
    ACTIVIDAD_CON_RESERVAS_PAGADAS("A.reservasPagadas", HttpStatus.CONFLICT,
            "Existen reservas asociadas en estado «Pagado». " +
            "Debe dirigirse al detalle de esta actividad y pasar todas las reservas a estado «Cancelado con reembolso» " +
            "y gestionar los reembolsos correspondientes antes de proceder."),
    ACTIVIDAD_CON_RESERVAS_ACTIVAS("A.conReservasActivas", HttpStatus.CONFLICT,
            "La actividad posee reservas en estado pendiente o pagada"),
    //US-ACT-11: Gestión de días
    DIA_HORARIO_PASADO("A.diaHorarioPasado", HttpStatus.BAD_REQUEST,
            "El horario de inicio seleccionado ya pasó."),
    DIA_FECHA_OCUPADA("A.diaFechaOcupada", HttpStatus.CONFLICT,
            "La actividad ya tiene un día activo en la fecha seleccionada."),
    LOTE_SIN_DIAS_PARA_CREAR("A.loteSinDias", HttpStatus.CONFLICT,
            "No hay días para crear en el rango seleccionado: todas las fechas ya tienen un día activo o su horario ya pasó."),
    DIA_NO_MODIFICABLE("A.diaNoModificable", HttpStatus.CONFLICT,
            "Solo se puede modificar el cupo de un día activo o reprogramado."),
    DIA_YA_COMENZO("A.diaYaComenzo", HttpStatus.CONFLICT,
            "No se puede modificar el cupo de un día que ya comenzó."),
    CUPO_MENOR_A_RESERVADOS("A.cupoMenorAReservados", HttpStatus.CONFLICT,
            "El cupo no puede ser menor a las personas con reserva vigente."),
    RANGO_FECHAS_INVALIDO("A.rangoFechasInvalido", HttpStatus.BAD_REQUEST,
            "La fecha hasta no puede ser anterior a la fecha desde."),
    FECHA_ANTERIOR_A_HOY("A.fechaAnteriorAHoy", HttpStatus.BAD_REQUEST,
            "La fecha no puede ser anterior a hoy."),
    FECHA_FUERA_DE_VENTANA("A.fechaFueraDeVentana", HttpStatus.BAD_REQUEST,
            "La fecha supera la ventana máxima permitida."),
    HORARIO_INVALIDO("A.horarioInvalido", HttpStatus.BAD_REQUEST,
            "La hora de fin debe ser posterior a la hora de inicio."),
    CALENDARIO_ANIO_INVALIDO("A.calendarioAnioInvalido", HttpStatus.BAD_REQUEST,
            "El año consultado está fuera del rango permitido.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
