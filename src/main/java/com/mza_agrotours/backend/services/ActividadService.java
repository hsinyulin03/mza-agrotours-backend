package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.entities.actividad.*;
import com.mza_agrotours.backend.dtos.reservas.DiaActividadReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.InfoParaReservarDTO;
import com.mza_agrotours.backend.dtos.reservas.RangoEtarioReservaDTO;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre;
import com.mza_agrotours.backend.enums.Dia;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.exceptions.EstablecimientoNotFoundException;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotActiveException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotFoundException;
import com.mza_agrotours.backend.exceptions.actividad.ValidacionMultipleException;
import com.mza_agrotours.backend.mappers.ActividadMapper;
import com.mza_agrotours.backend.mappers.ArchivoMapper;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.TipoCultivo.TipoCultivoRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRespository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadDiaRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ActividadService {
    private final List<String> EXTENSIONES_VALIDAS = List.of("jpg", "jpeg", "png");
    @Autowired
    private ActividadRespository actividadRepository;

    @Autowired
    private ActividadValidaciones actividadValidaciones;

    @Autowired
    private ActividadMapper actividadMapper;

    @Autowired
    private EstadoActividadRepository estadoActividadRepository;

    @Autowired
    private EstadoActividadDiaRepository estadoActividadDiaRepository;

    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private ParametrosService parametrosService;

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ArchivoService archivoService;

    @Autowired
    private ArchivoMapper archivoMapper;

    //US-ACT-03 Alta de actividad
    @Transactional
    public DTOActividadAltaResponse altaActividad(DTOActividadAlta dto) {

        //Primero hacemos las validaciones del negocio
        List<String> errores = actividadValidaciones.obtenerErroresValidacionActividad(dto);

        if (!errores.isEmpty()) {
            throw new ValidacionMultipleException(errores);
        }

        EstadoActividad estado = obtenerEstado(dto.getEstado());

        //Paso 1: Información General
        Actividad actividad = new Actividad();
        actividad.setNombre(dto.getNombre());
        actividad.setDescripcion(dto.getDescripcion());
        actividad.setCuposMax(dto.getCuposMax());
        //Guarda el UUID del estado
        actividad.setEstado(estado);

        List<TipoCultivo> cultivos = obtenerCultivos(dto.getCultivos());
        actividad.setCultivos(cultivos);

        List<ActividadInclusiones> inclusiones = obtenerInclusiones(dto.getIncluye(), dto.getNoIncluye());
        List<ActividadFAQ> faqs = obtenerFaqs(dto.getFaqs());
        List<ActividadRangoEtario> tarifas = obtenerTarifas(dto.getTarifas());
        ActividadLogAltas logAltas = obtenerLogAltas(dto);
        List<ActividadDia> calendario = generarDiasCalendario(dto, logAltas);

        //setear los valores obtenidos a actividad
        inclusiones.forEach(actividad::addInclusion);
        faqs.forEach(actividad::addFaq);
        tarifas.forEach(actividad::addActividadRangoEtario);
        actividad.addLogAlta(logAltas);
        calendario.forEach(actividad::addActividadDia);

        List<ArchivoUploadResponse> urlsGeneradas = new ArrayList<>();

        if (dto.getFotos() != null && !dto.getFotos().isEmpty()) {
            // Pedimos las URLs firmadas
            urlsGeneradas = archivoService.getSignedArchivos(dto.getFotos(), EXTENSIONES_VALIDAS);

            List<Archivo> entidadesArchivo = this.archivoMapper.archivoUploadResponseListToArchivoList(urlsGeneradas);

            entidadesArchivo.forEach(actividad::addFoto);
        }

        // Persistir en la base de datos
        Actividad actividadGuardada = actividadRepository.save(actividad);

        List<String> advertencias = calcularHuecos(dto.getTarifas());

        DTOActividadAltaResponse response = new DTOActividadAltaResponse();
        response.setIdActividad(actividadGuardada.getId());
        response.setMensaje("La actividad fue creada exitosamente.");
        response.setAdvertencias(advertencias);
        response.setArchivoUploadResponses(urlsGeneradas);

        return response;
    }

    //US-ACT-02:  Consultar detalle de una actividad
    @Transactional(readOnly = true)
    public DTOActividadDetalleResponse obtenerDetallePorId(UUID idActividad) {
        Actividad actividad = obtenerActividad(idActividad);
        DTOActividadDetalleResponse response = actividadMapper.actividadToDTOActividadDetalle(actividad);
        response.setFotos(obtenerUrlsDeDescarga(response.getFotos()));
        return response;
    }

    //US-ACT-06: Listado de actividades de un establecimiento - Vista productor
    @Transactional(readOnly = true)
    public List<DTOActividadesResponse> obtenerListadoActividades(String busqueda, EstadoActividadNombre estado) {
        // TODO- Se debe filtrar por establecimiento
        List<Actividad> actividades = actividadRepository.findByFiltrosDinamicos(busqueda, estado);

        return actividades.stream()
                .map(actividadMapper::actividadToDTOActividades)
                .toList();
    }

    //US-ACT-07: Consultar todos los días disponibles para una actividad
    @Transactional(readOnly = true)
    public DTOCalendarioActividadDiaResponse obtenerDetalleCalendario(UUID idActividad, int mes, int anio){

        Actividad actividad = obtenerActividad(idActividad);
        int anioActual = java.time.LocalDate.now().getYear();

        if (anio < anioActual) {
            throw new ValidacionNegocioException("El año no puede ser menor al año actual (" + anioActual + ")");
        }

        LocalDateTime ultimaFechaConDisponibilidad = actividadRepository.findUltimaFechaByActividadId(idActividad)
                .orElseThrow(() -> new ValidacionNegocioException("La actividad no tiene días programados"));


        int anioMaximoPermitido = ultimaFechaConDisponibilidad.getYear();
        if (anio > anioMaximoPermitido) {
            throw new ValidacionNegocioException("No puedes consultar el calendario para el año " + anio +
                    ". La actividad tiene disponibilidad cargada solo hasta el año " + anioMaximoPermitido);
        }

        DTOCalendarioActividadDiaResponse dto = actividadMapper.actividadToDTOCalendarioActividadDia(actividad);

        List<DTOActividadDiaResponse> diasDelMesDto = actividad.getActividadesDias().stream()
                .filter(dia -> dia.getFechaHoraInicio() != null)
                .filter(dia -> dia.getFechaHoraInicio().getYear() == anio
                        && dia.getFechaHoraInicio().getMonthValue() == mes)
                .map(actividadMapper::actividadDiatoDTOActividadDia)
                .toList();

        dto.setDiasDelMes(diasDelMesDto);

        return dto;
    }

    //US-ACT-12: Listado de actividades de la plataforma - vista del visitante
    @Transactional(readOnly = true)
    public List<DTOListadoActividadVisitanteResponse> explorarActividades(List<UUID> cultivoIds) {
        List<Actividad> actividades;

        // TODO: Falta implementar filtro por departamento
        // TODO: Falta implementar paginación

        if (cultivoIds == null || cultivoIds.isEmpty()) {
            actividades = actividadRepository.explorarActividadesPublicadas();
        }else {
            actividades = actividadRepository.explorarActividadesPublicadas(cultivoIds);
        }
        List<DTOListadoActividadVisitanteResponse> response = actividades.stream().map(actividad -> {

            DTOListadoActividadVisitanteResponse dto = actividadMapper.actividadToDTOListadoActividadVisitante(actividad);

            if (actividad.getFotos() != null && !actividad.getFotos().isEmpty()) {
                Archivo primeraFoto = actividad.getFotos().get(0);
                DTOFotosResponse fotoDto = new DTOFotosResponse();
                fotoDto.setKey(primeraFoto.getKey());
                fotoDto.setNombre(primeraFoto.getNombre());
                fotoDto.setExtension(primeraFoto.getExtension());
                fotoDto.setDownloadUrl(archivoService.getDownloadUrl(primeraFoto.getKey()));
                dto.setFotoPortada(fotoDto);
            }
            return dto;
        }).toList();

        return response;
    }

    //US-ACT-04: Modificar Actividad
    @Transactional
    public DTOActividadGetResponse modificarActividad(UUID idActividad, DTOActividadUpdate dto) {

        Actividad actividad = obtenerActividad(idActividad);

        List<String> errores = actividadValidaciones.obtenerErroresValidacionModificacion(idActividad, dto);

        if (!errores.isEmpty()) {
            throw new ValidacionMultipleException(errores);
        }

        EstadoActividad nuevoEstado = obtenerEstado(dto.getEstado());
        if (EstadoActividadNombre.BORRADOR.name().equalsIgnoreCase(dto.getEstado()) &&
                tieneReservasPendientesOPagadas(idActividad)) {

            throw new ValidacionNegocioException("No se permite cambiar a estado borrador: la actividad posee reservas en estado pendiente o pagada.");
        }

        actividad.setEstado(nuevoEstado);

        actividad.setNombre(dto.getNombre());
        actividad.setDescripcion(dto.getDescripcion());

        List<TipoCultivo> cultivos = actualizarCultivos(actividad.getCultivos(), dto.getCultivos());
        actividad.getCultivos().clear();
        actividad.getCultivos().addAll(cultivos);

        List<ActividadRangoEtario> activosActuales = actividad.getActividadRangoEtarios().stream()
                .filter(r -> r.getFechaHoraBaja() == null)
                .collect(Collectors.toList());

        List<ActividadRangoEtario> nuevasTarifas = actualizarTarifas(dto.getTarifas(), activosActuales);

        nuevasTarifas.forEach(actividad::addActividadRangoEtario);

        actividad.getInclusiones().clear();
        List<ActividadInclusiones> nuevasInclusiones = obtenerInclusiones(dto.getIncluye(), dto.getNoIncluye());
        actividad.getInclusiones().addAll(nuevasInclusiones);

        actividad.getFaqs().clear();
        List<ActividadFAQ> nuevasFaqs = obtenerFaqs(dto.getFaqs());
        actividad.getFaqs().addAll(nuevasFaqs);

        if (dto.getFotosExistentes() != null) {
            actividad.getFotos().removeIf(foto -> !dto.getFotosExistentes().contains(foto.getKey()));
        }

        List<ArchivoUploadResponse> urlsGeneradas = new ArrayList<>();

        if (dto.getFotosNuevas() != null && !dto.getFotosNuevas().isEmpty()) {
            // Pasamos la lista de ArchivoUploadRequest al servicio para que nos dé las URLs de subida
            urlsGeneradas = archivoService.getSignedArchivos(dto.getFotosNuevas(), EXTENSIONES_VALIDAS);
            List<Archivo> entidadesArchivoNuevas = archivoMapper.archivoUploadResponseListToArchivoList(urlsGeneradas);
            entidadesArchivoNuevas.forEach(actividad::addFoto);
        }

        Actividad actividadGuardada = actividadRepository.save(actividad);
        DTOActividadGetResponse response = actividadMapper.actividadToDTOActividadGetResponse(actividadGuardada);

        // Inyectamos la URL de DESCARGA (GET) a TODAS las fotos de la respuesta
        response.setFotosGuardadas(obtenerUrlsDeDescarga(response.getFotosGuardadas()));

        // Adjuntamos las URLs de SUBIDA (PUT) para que el front cargue las fotos nuevas
        response.setFotosParaSubir(urlsGeneradas);
        List<String> advertencias = calcularHuecos(dto.getTarifas());
        response.setAdvertencias(advertencias);

        return response;

    }
    @Transactional(readOnly = true)
    public DTOActividadGetResponse obtenerActividadPorId(UUID idActividad) {
        Actividad actividad = obtenerActividad(idActividad);
        DTOActividadGetResponse response = actividadMapper.actividadToDTOActividadGetResponse(actividad);

        response.setFotosGuardadas(obtenerUrlsDeDescarga(response.getFotosGuardadas()));
        return response;
    }

    //Métodos auxiliares

    private EstadoActividad obtenerEstado(String nombreEstadoDto) {
        EstadoActividadNombre estadoActividadNombre;
        try {
            estadoActividadNombre = EstadoActividadNombre.valueOf(nombreEstadoDto.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("El estado de actividad proporcionado es inválido: " + nombreEstadoDto);
        }
        return estadoActividadRepository.findByNombre(estadoActividadNombre)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el registro del estado " + estadoActividadNombre + " en la base de datos."));
    }

    private ActividadLogAltas obtenerLogAltas(DTOActividadAlta dto) {
        ActividadLogAltas logAltas = new ActividadLogAltas();
        logAltas.setFechaHoraAlta(LocalDateTime.now());
        logAltas.setFechaValidaDesde(dto.getFechaDesde());
        logAltas.setFechaValidaHasta(dto.getFechaHasta());

        if (dto.getDiasDisponibles() != null) {
            // Recorremos los días que el usuario seleccionó en la pantalla
            for (DTODiaDisponibilidad diaDto : dto.getDiasDisponibles()) {
                ActividadLogAltasDia dia = new ActividadLogAltasDia();
                dia.setDia(diaDto.getDia());
                dia.setHoraInicio(diaDto.getHoraInicio());
                dia.setHoraFin(diaDto.getHoraFin());
                logAltas.addDia(dia);
            }
        }
        return logAltas;
    }

    //Método para crear las ActividadDia
    private List<ActividadDia> generarDiasCalendario(DTOActividadAlta dto, ActividadLogAltas logAltas) {
        List<ActividadDia> diasGenerados = new ArrayList<>();
        LocalDate fechaActual = dto.getFechaDesde();
        LocalDate limite = dto.getFechaHasta();

        EstadoActividadDia estadoActivaEntidad = estadoActividadDiaRepository.findByNombre(EstadoActividadDiaNombre.ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("El estado ACTIVA no está configurado en la base de datos de catálogos."));

        while (!fechaActual.isAfter(limite)) {
            java.time.DayOfWeek diaSemanaActual = fechaActual.getDayOfWeek();

            for (ActividadLogAltasDia configDia : logAltas.getDias()) {
                if (coincideDia(configDia.getDia(), diaSemanaActual)) {

                    // Calculamos la fecha y hora exacta de inicio de este turno
                    LocalDateTime inicioCalculado = LocalDateTime.of(fechaActual, configDia.getHoraInicio());

                    // Evitar crear disponibilidades cuya hora de inicio ya haya pasado.
                    if (!inicioCalculado.isAfter(LocalDateTime.now())) {
                        continue; // Salta este horario y sigue buscando
                    }

                    ActividadDia actividadDia = new ActividadDia();
                    actividadDia.setFechaHoraInicio(LocalDateTime.of(fechaActual, configDia.getHoraInicio()));
                    actividadDia.setFechaHoraFin(LocalDateTime.of(fechaActual, configDia.getHoraFin()));
                    actividadDia.setCuposMax(dto.getCuposMax());

                    ActividadDiaEstado estadoInicial = new ActividadDiaEstado();
                    estadoInicial.setEstado(estadoActivaEntidad);
                    estadoInicial.setFechaHoraInicio(LocalDateTime.now());
                    actividadDia.registrarNuevoEstado(estadoInicial);

                    configDia.addActividadDia(actividadDia);
                    diasGenerados.add(actividadDia);
                    break;
                }
            }
            fechaActual = fechaActual.plusDays(1);
        }

        if (diasGenerados.isEmpty()) {
            throw new ValidacionNegocioException(" El rango de fechas seleccionado ("
                    + dto.getFechaDesde() + " al " + dto.getFechaHasta() +
                    ") no contiene ninguno de los días de la semana configurados.");
        }
        return diasGenerados;
    }

    private boolean coincideDia(Dia diaEnum, java.time.DayOfWeek dayOfWeek) {
        return switch (diaEnum) {
            case LUNES -> dayOfWeek == java.time.DayOfWeek.MONDAY;
            case MARTES -> dayOfWeek == java.time.DayOfWeek.TUESDAY;
            case MIERCOLES -> dayOfWeek == java.time.DayOfWeek.WEDNESDAY;
            case JUEVES -> dayOfWeek == java.time.DayOfWeek.THURSDAY;
            case VIERNES -> dayOfWeek == java.time.DayOfWeek.FRIDAY;
            case SABADO -> dayOfWeek == java.time.DayOfWeek.SATURDAY;
            case DOMINGO -> dayOfWeek == java.time.DayOfWeek.SUNDAY;
            default -> false;
        };
    }

    private List <ActividadRangoEtario> actualizarTarifas(List<DTOTarifa> nuevosRangos,List<ActividadRangoEtario> activosActuales) {
        List<UUID> idsValidos = activosActuales.stream()
                .map(ActividadRangoEtario::getId)
                .toList();

        for (DTOTarifa dto : nuevosRangos) {
            if (dto.getId() != null && !idsValidos.contains(dto.getId())) {
                throw new ValidacionNegocioException(
                        "El ID de tarifa proporcionado (" + dto.getId() + ") no es válido o no pertenece a esta actividad."
                );
            }
        }

        List<ActividadRangoEtario> aDarDeBaja = new ArrayList<>();
        List<DTOTarifa> aInsertar = new ArrayList<>(nuevosRangos);

        for (ActividadRangoEtario activo : activosActuales) {
            Optional<DTOTarifa> match = aInsertar.stream()
                    .filter(dto -> (dto.getId() != null && dto.getId().equals(activo.getId())) ||
                            (dto.getNombre() != null && dto.getNombre().equalsIgnoreCase(activo.getNombre()) &&
                                    (dto.getEdadMinima() != null && dto.getEdadMinima().equals(activo.getEdadMinima()))&&
                                    (dto.getEdadMaxima() != null && dto.getEdadMaxima().equals(activo.getEdadMaxima())) &&
                                    dto.getPrecio().compareTo(activo.getPrecio()) == 0)
                    )
                    .findFirst();

            if (match.isPresent()) {
                DTOTarifa dto = match.get();

                // Cambio que requiere historial (Precio ||  Edades)
                boolean requiereNuevoHistorial = dto.getPrecio().compareTo(activo.getPrecio()) != 0 ||
                        !dto.getEdadMinima().equals(activo.getEdadMinima()) ||
                        !dto.getEdadMaxima().equals(activo.getEdadMaxima());

                if (requiereNuevoHistorial) {
                    // Si cambia el precio o el rango etario. Damos de baja el registro viejo para evitar inconsistencias en las reservas.
                    aDarDeBaja.add(activo);

                } else {
                    activo.setNombre(dto.getNombre());
                    activo.setEsTarifaBase(dto.isEsTarifaBase());
                    aInsertar.remove(dto);
                }

            } else {
                aDarDeBaja.add(activo);
            }
        }

        LocalDateTime ahora = LocalDateTime.now();
        aDarDeBaja.forEach(r -> r.setFechaHoraBaja(ahora));

        return  obtenerTarifas(aInsertar);
    }
    //Paso 3: Participanetes y tarifas
    private List<ActividadRangoEtario> obtenerTarifas(List<DTOTarifa> dtosTarifa) {
        if (dtosTarifa == null || dtosTarifa.isEmpty()) {
            return Collections.emptyList();
        }

        List<ActividadRangoEtario> tarifas = new ArrayList<>();

        for (DTOTarifa tarifaDto : dtosTarifa) {

            ActividadRangoEtario tarifa = new ActividadRangoEtario();
            tarifa.setNombre(tarifaDto.getNombre());
            tarifa.setPrecio(tarifaDto.getPrecio());
            tarifa.setEdadMinima(tarifaDto.getEdadMinima());
            tarifa.setEdadMaxima(tarifaDto.getEdadMaxima());
            tarifa.setEsTarifaBase(tarifaDto.isEsTarifaBase());
            tarifas.add(tarifa);
        }

        return tarifas;
    }

    private List<ActividadInclusiones> obtenerInclusiones(List<String> incluye, List<String> noIncluye) {
        List<ActividadInclusiones> inclusiones = new ArrayList<>();
        if (incluye != null) {
            for (String desc : incluye) {
                ActividadInclusiones inclusion = new ActividadInclusiones();
                inclusion.setDescripcion(desc);
                inclusion.setIncluye(true);
                inclusiones.add(inclusion);
            }
        }
        if (noIncluye != null) {
            for (String desc : noIncluye) {
                ActividadInclusiones exclusion = new ActividadInclusiones();
                exclusion.setDescripcion(desc);
                exclusion.setIncluye(false);
                inclusiones.add(exclusion);
            }
        }
        return inclusiones;
    }

    private List<ActividadFAQ> obtenerFaqs(List<DTOFaq> dtosFaq) {
        if (dtosFaq == null) {
            return Collections.emptyList();
        }

        List<ActividadFAQ> faqs = new ArrayList<>();

        for (DTOFaq faqDto : dtosFaq) {
            ActividadFAQ faq = new ActividadFAQ();
            faq.setPregunta(faqDto.getPregunta());
            faq.setRespuesta(faqDto.getRespuesta());
            faqs.add(faq);
        }
        return faqs;
    }

    private List<String> calcularHuecos(List<DTOTarifa> tarifas) {

        List<String> huecos = new ArrayList<>();

        if (tarifas == null || tarifas.isEmpty()) {
            huecos.add("0 a 120 años");
            return huecos;
        }

        // Ordenamos por edad mínima de menor a mayor
        List<DTOTarifa> tarifasOrdenadas = new ArrayList<>(tarifas);
        tarifasOrdenadas.sort(Comparator.comparingInt(DTOTarifa::getEdadMinima));

        // Verificamos el hueco inicial
        DTOTarifa primerRango = tarifasOrdenadas.get(0);
        if (primerRango.getEdadMinima() > 0) {
            int finHueco = primerRango.getEdadMinima() - 1;
            huecos.add("0 a " + finHueco + " años");
        }

        // Verificamos los huecos intermedios
        for (int i = 0; i < tarifasOrdenadas.size() - 1; i++) {
            int maxActual = tarifasOrdenadas.get(i).getEdadMaxima();
            int minSiguiente = tarifasOrdenadas.get(i + 1).getEdadMinima();

            if (minSiguiente > maxActual + 1) {
                int inicioHueco = maxActual + 1;
                int finHueco = minSiguiente - 1;
                huecos.add(inicioHueco + " a " + finHueco + " años");
            }
        }

        // Verificamos el hueco final
        DTOTarifa ultimoRango = tarifasOrdenadas.get(tarifasOrdenadas.size() - 1);
        //TODO: Setear como parámetro global del sistema
        int EDAD_MAXIMA_SISTEMA = 120;

        if (ultimoRango.getEdadMaxima() < EDAD_MAXIMA_SISTEMA) {
            int inicioHueco = ultimoRango.getEdadMaxima() + 1;
            huecos.add(inicioHueco + " a " + EDAD_MAXIMA_SISTEMA + " años");
        }

        return huecos;
    }
    private List<TipoCultivo> obtenerCultivos(List<UUID> idCultivos) {

        if (idCultivos == null || idCultivos.isEmpty()) {
            throw new ValidacionNegocioException("El tipo de cultivo es requerido");
        }

        // Asegura de no tener IDs repetidos en la request, si los tiene los elimina
        List<UUID> idsUnicos = idCultivos.stream().distinct().toList();
        List<TipoCultivo> cultivosActivos = tipoCultivoRepository.findActivosByIds(idsUnicos);

        if (cultivosActivos.size() != idsUnicos.size()) {
            throw new ValidacionNegocioException("Uno o más tipos de cultivo seleccionados no existen o se encuentran dados de baja.");
        }
        return cultivosActivos;
    }

    private List<TipoCultivo> actualizarCultivos(List <TipoCultivo> cultivosActuales, List<UUID> idsRequest) {
        // Obtener IDs de los cultivos que la actividad ya tiene asignados
        List<UUID> idsActuales = cultivosActuales.stream()
                .map(TipoCultivo::getId)
                .collect(Collectors.toList());

        // Separar los IDs recibidos en el dto en "nuevos" y "mantenidos"
        List<UUID> idsNuevos = idsRequest.stream()
                .filter(id -> !idsActuales.contains(id))
                .distinct()
                .collect(Collectors.toList());

        List <UUID> idsMantenidos = idsRequest.stream()
                .filter(idsActuales::contains)
                .distinct()
                .collect(Collectors.toList());

        List<TipoCultivo> cultivosDefinitivos = new ArrayList<>();

        // Validar y recuperar los cultivos nuevos (no deben estar dados de baja)
        if (!idsNuevos.isEmpty()) {
            List<TipoCultivo> cultivosNuevos = tipoCultivoRepository.findActivosByIds(idsNuevos);
            if (cultivosNuevos.size() != idsNuevos.size()) {
                throw new ValidacionNegocioException("Uno o más tipos de cultivo seleccionados se encuentran dados de baja.");
            }
            cultivosDefinitivos.addAll(cultivosNuevos);
        }

        // Si ya lo tenía desde antes esos cultivos, se lo dejamos guardar (sin importar si está dado de baja)
        if (!idsMantenidos.isEmpty()) {
            cultivosActuales.stream()
                    .filter(c -> idsMantenidos.contains(c.getId()))
                    .forEach(cultivosDefinitivos::add);
        }
        return cultivosDefinitivos;
    }

    private boolean tieneReservasPendientesOPagadas(UUID idActividad) {
        List<EstadoReservaNombre> estadosQueBloquean = List.of(
                EstadoReservaNombre.PENDIENTE,
                EstadoReservaNombre.PAGADA
        );

        return reservaRepository.existsByActividadIdAndEstadoActualEstadoReservaNombreIn(
                idActividad,
                estadosQueBloquean
        );
    }
    private List<DTOFotosResponse> obtenerUrlsDeDescarga(List<DTOFotosResponse> fotos) {
        if (fotos != null && !fotos.isEmpty()) {
            fotos.forEach(foto ->
                    foto.setDownloadUrl(archivoService.getDownloadUrl(foto.getKey()))
            );
        }
        return fotos; // Retornamos la misma lista, pero con las URLs cargadas
    }
    private Actividad obtenerActividad(UUID idActividad){
        return actividadRepository.findByIdAndFechaHoraBajaIsNull(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con ID: " + idActividad));
    }


    //US-RESE-01: Reservar actividad - información sobre la actividad para reservarla
    @Transactional
    public InfoParaReservarDTO getInfoParaReservar(UUID idActividad){

        LocalDateTime fhActual = LocalDateTime.now();

        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(ActividadNotFoundException::new);

        // Que la actividad esté disponible (estado Publicado)
        if (actividad.getEstado().getNombre() != EstadoActividadNombre.PUBLICADO)
            throw new ActividadNotActiveException();

        // Buscar el Establecimiento de la actividad
        Establecimiento establecimiento = establecimientoRepository.findEstablecimientoByActividadId(actividad.getId())
                .orElseThrow(EstablecimientoNotFoundException::new);

        // Encontramos los ActividadDia y lo pasamos a DTO
        List<DiaActividadReservaDTO> diaActividadReservaDTOList = actividadRepository.getDiaActividadReservaDTO(actividad.getId());

        // ActividadRangoEtario activos
        List<ActividadRangoEtario> areActivos = actividad.getActividadRangoEtarios().stream()
                .filter(are -> {
                    LocalDateTime areFHBaja = are.getFechaHoraBaja();
                    // La fechaHoraBaja es posterior a la actual o es nula
                    return areFHBaja == null || areFHBaja.isAfter(fhActual);
                })
                .toList();
        // Los pasamos a DTO
        List<RangoEtarioReservaDTO> rangoEtarioReservaDTOList = new ArrayList<>();
        for (ActividadRangoEtario are : areActivos){
            rangoEtarioReservaDTOList.add(actividadMapper.actividadRangoEtarioToDTO(are));
        }

        //Armar el DTO principal y devolver
        return InfoParaReservarDTO.of(
                actividad,
                establecimiento,
                diaActividadReservaDTOList,
                rangoEtarioReservaDTOList,
                parametrosService.getInstance().getDiasMinReembolso());
    }
}





