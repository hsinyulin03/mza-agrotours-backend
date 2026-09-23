package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.DTOFotosResponse;
import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.entities.ActividadFoto;
import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.*;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.*;
import com.mza_agrotours.backend.exceptions.*;
import com.mza_agrotours.backend.exceptions.actividad.ActividadError;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotActiveException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotFoundException;
import com.mza_agrotours.backend.exceptions.actividad.ValidacionMultipleException;
import com.mza_agrotours.backend.mappers.ActividadMapper;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.TipoCultivo.TipoCultivoRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadDiaRepository;
import com.mza_agrotours.backend.repositories.actividad.EstadoActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ActividadService {
    @Autowired
    private ActividadRepository actividadRepository;

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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VisitanteRepository visitanteRepository;

    @Autowired
    private ArchivoService archivoService;

    @Autowired
    private ReservaService reservaService;

    //US-ACT-03 Alta de actividad
    @Transactional
    public DTOActividadAltaResponse altaActividad(UUID establecimientoId, DTOActividadAlta dto) {
        validarEstablecimientoNoSuspendido(establecimientoId);

        //Primero hacemos las validaciones del negocio
        List<String> errores = actividadValidaciones.obtenerErroresValidacionActividad(establecimientoId, dto);

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

        Establecimiento establecimiento = establecimientoRepository.findByIdAndFechaHoraBajaIsNull(establecimientoId)
                .orElseThrow(EstablecimientoNotFoundException::new);

        agregarCultivosAEstablecimiento(establecimiento, cultivos);
        establecimientoRepository.save(establecimiento);

        actividad.setEstablecimiento(establecimiento);

        sincronizarFotos(dto.getFotos().stream().map(archivoReq -> new DTOActividadFotoReq(archivoReq.getKey(), archivoReq.getNombre())).toList(), actividad);

        // Persistir en la base de datos
        Actividad actividadGuardada = actividadRepository.save(actividad);

        List<String> advertencias = calcularHuecos(dto.getTarifas());

        DTOActividadAltaResponse response = new DTOActividadAltaResponse();
        response.setIdActividad(actividadGuardada.getId());
        response.setMensaje("La actividad fue creada exitosamente.");
        response.setAdvertencias(advertencias);

        return response;
    }

    //US-ACT-02:  Consultar detalle de una actividad
    @Transactional(readOnly = true)
    public DTOActividadDetalleResponse obtenerDetallePorId(UUID idActividad) {
        Actividad actividad = this.actividadRepository.findByIdVigenteConEstablecimientoActivo(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada con ID: " + idActividad));
        DTOActividadDetalleResponse response = actividadMapper.actividadToDTOActividadDetalle(actividad);
        response.setFotos(obtenerUrlsDeDescarga(response.getFotos()));
        return response;
    }

    //US-ACT-06: Listado de actividades de un establecimiento - Vista productor
    @Transactional(readOnly = true)
    public Page<DTOActividadesResponse> obtenerListadoActividades(UUID establecimientoId, String busqueda, EstadoActividadNombre estado, Pageable pageable) {
        String texto = (busqueda == null || busqueda.isBlank()) ? null : busqueda.trim();
        Page<Actividad> actividades = actividadRepository.findByFiltrosDinamicos(establecimientoId, texto, estado, pageable);

        return actividades.map(actividadMapper::actividadToDTOActividades);
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

        LocalDateTime desde = LocalDate.of(anio, mes, 1).atStartOfDay();
        LocalDateTime hasta = desde.plusMonths(1);

        List<ActividadDia> diasDelMes = actividadRepository.findDiasDelMes(idActividad, desde, hasta);
        Map<UUID, DTOCuposPorDia> cuposPorDia = obtenerCuposPorDia(diasDelMes);

        List<DTOActividadDiaResponse> diasDelMesDto = diasDelMes.stream()
                .map(dia -> {
                    DTOActividadDiaResponse dtoDia = actividadMapper.actividadDiatoDTOActividadDia(dia);
                    dtoDia.aplicarCupos(cuposPorDia.get(dia.getId()));
                    return dtoDia;
                })
                .toList();

        dto.setDiasDelMes(diasDelMesDto);
        dto.setMetricas(reservaRepository.obtenerMetricasDeReservas(idActividad));
        return dto;
    }

    //US-ACT-12: Listado de actividades de la plataforma - vista del visitante
    @Transactional(readOnly = true)
    public Page<DTOListadoActividadVisitanteResponse> explorarActividades(String busqueda, List<UUID> cultivoIds, UUID departamentoId, Pageable pageable) {

        String texto = (busqueda == null || busqueda.isBlank()) ? null : busqueda.trim();
        List<UUID> cultivosId = (cultivoIds == null || cultivoIds.isEmpty()) ? null : cultivoIds;
        Page<Actividad> actividadesPage = actividadRepository.explorarActividadesPublicadas(texto, cultivosId, departamentoId, pageable);

        return actividadesPage.map(actividad -> {

            DTOListadoActividadVisitanteResponse dto = actividadMapper.actividadToDTOListadoActividadVisitante(actividad);

            if (actividad.getFotos() != null && !actividad.getFotos().isEmpty()) {
                Archivo primeraFoto = actividad.getFotos().get(0).getArchivo();
                DTOFotosResponse fotoDto = new DTOFotosResponse();
                fotoDto.setKey(primeraFoto.getKey());
                fotoDto.setNombre(primeraFoto.getNombre());
                fotoDto.setExtension(primeraFoto.getExtension());
                fotoDto.setDownloadUrl(archivoService.getDownloadUrl(primeraFoto.getKey()));
                dto.setFotoPortada(fotoDto);
            }
            return dto;
        });

    }

    //US-ACT-04: Modificar Actividad
    @Transactional
    public DTOActividadGetResponse modificarActividad(UUID idEstablecimiento, UUID idActividad, DTOActividadUpdate dto) {
        validarEstablecimientoNoSuspendido(idEstablecimiento);

        Actividad actividad = obtenerActividad(idActividad);

        List<String> errores = actividadValidaciones.obtenerErroresValidacionModificacion(idEstablecimiento, idActividad, dto);

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

        List<TipoCultivo> cultivosAnteriores = new ArrayList<>(actividad.getCultivos());

        List<TipoCultivo> cultivos = actualizarCultivos(actividad.getCultivos(), dto.getCultivos());
        actividad.getCultivos().clear();
        actividad.getCultivos().addAll(cultivos);

        List<TipoCultivo> cultivosRemovidos = cultivosAnteriores.stream()
                .filter(cultivoAnterior -> cultivos.stream().noneMatch(c -> c.getId().equals(cultivoAnterior.getId())))
                .toList();

        Establecimiento establecimiento = establecimientoRepository.findByIdAndFechaHoraBajaIsNull(idEstablecimiento)
                .orElseThrow(EstablecimientoNotFoundException::new);

        agregarCultivosAEstablecimiento(establecimiento, cultivos);
        quitarCultivosSinUsoDelEstablecimiento(establecimiento, cultivosRemovidos, idActividad);
        establecimientoRepository.save(establecimiento);

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

        sincronizarFotos(dto.getFotos(), actividad);

        Actividad actividadGuardada = actividadRepository.save(actividad);
        DTOActividadGetResponse response = actividadMapper.actividadToDTOActividadGetResponse(actividadGuardada);

        // Inyectamos la URL de DESCARGA (GET) a TODAS las fotos de la respuesta
        response.setFotosGuardadas(obtenerUrlsDeDescarga(response.getFotosGuardadas()));

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

    @Transactional(readOnly = true)
    public List<DTOFiltro> obtenerFiltroEstadoActividad(UUID idEstablecimiento) {

        List<DTOFiltro> resultadosBD = actividadRepository.contarActividadesPorEstado(idEstablecimiento);

        Map<String, Long> mapaCantidades = resultadosBD.stream()
                .collect(Collectors.toMap(
                        DTOFiltro::getValor,
                        DTOFiltro::getCantidad
                ));

        return Arrays.stream(EstadoActividadNombre.values())
                .map(estado -> {
                    DTOFiltro dto = new DTOFiltro();
                    dto.setNombre(estado.getNombre());  // nombre: "Borrador"
                    dto.setValor(estado.name());   // valor: "BORRADOR"
                    dto.setCantidad(mapaCantidades.getOrDefault(estado.name(), 0L));
                    return dto;
                })
                .toList();

    }
    @Transactional(readOnly = true)
    public List<DTOFiltro> obtenerFiltroDepartamentos() {
        return actividadRepository.obtenerFiltroDepartamentos();
    }

    @Transactional(readOnly = true)
    public List<DTOFiltro> obtenerFiltroCultivos() {
        return actividadRepository.obtenerFiltroCultivos();
    }


    @Transactional
    public DTOBajaActividadResponse darBajaActividad(UUID idEstablecimiento, UUID idActividad){
        validarEstablecimientoNoSuspendido(idEstablecimiento);
        Actividad actividad = obtenerActividad(idActividad);

        LocalDateTime ahora = LocalDateTime.now();

        if (reservaRepository.existeReservaPagadaFuturaByActividadId(idActividad, ahora)) {
            throw new AppException(ActividadError.ACTIVIDAD_CON_RESERVAS_PAGADAS);
        }
        actividad.setEstado(obtenerEstado(EstadoActividadNombre.DADO_DE_BAJA.name()));
        actividad.setFechaHoraBaja(ahora);
        cancelarDiasFuturos(idActividad, ahora);
        actividadRepository.save(actividad);

        reservaService.cancelarReservasPorBajaDeActividad(actividad, ahora);
        return actividadMapper.actividadToDTOBajaActividad(actividad);
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
        LocalDateTime ahora = LocalDateTime.now();

        EstadoActividadDia estadoActivaEntidad = estadoActividadDiaRepository.findByNombre(EstadoActividadDiaNombre.ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("El estado ACTIVA no está configurado en la base de datos de catálogos."));

        while (!fechaActual.isAfter(limite)) {
            java.time.DayOfWeek diaSemanaActual = fechaActual.getDayOfWeek();

            for (ActividadLogAltasDia configDia : logAltas.getDias()) {
                if (coincideDia(configDia.getDia(), diaSemanaActual)) {

                    // Calculamos la fecha y hora exacta de inicio de este turno
                    LocalDateTime inicioCalculado = LocalDateTime.of(fechaActual, configDia.getHoraInicio());

                    // Evitar crear disponibilidades cuya hora de inicio ya haya pasado.
                    if (!inicioCalculado.isAfter(ahora)) {
                        continue; // Salta este horario y sigue buscando
                    }

                    ActividadDia actividadDia = new ActividadDia();
                    actividadDia.setFechaHoraInicio(LocalDateTime.of(fechaActual, configDia.getHoraInicio()));
                    actividadDia.setFechaHoraFin(LocalDateTime.of(fechaActual, configDia.getHoraFin()));
                    actividadDia.setCuposMax(dto.getCuposMax());

                    actividadDia.cambiarEstado(estadoActivaEntidad, ahora, "Alta de la actividad");

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

    // Asocia al establecimiento los cultivos de la actividad que aún no tenga asignados.
    // Si el establecimiento ya tiene el cultivo, no se hace nada.
    private void agregarCultivosAEstablecimiento(Establecimiento establecimiento, List<TipoCultivo> cultivos) {
        List<UUID> idsCultivosActuales = establecimiento.getTiposCultivos().stream()
                .map(TipoCultivo::getId)
                .toList();

        List<TipoCultivo> cultivosNuevos = cultivos.stream()
                .filter(cultivo -> !idsCultivosActuales.contains(cultivo.getId()))
                .toList();

        establecimiento.getTiposCultivos().addAll(cultivosNuevos);
    }

    // Ante la quita de un cultivo de una actividad, sólo se desasocia del establecimiento
    // si ninguna otra actividad vigente (no dada de baja) del establecimiento lo sigue utilizando.
    private void quitarCultivosSinUsoDelEstablecimiento(Establecimiento establecimiento, List<TipoCultivo> cultivosRemovidos, UUID idActividadActual) {
        if (cultivosRemovidos == null || cultivosRemovidos.isEmpty()) {
            return;
        }

        List<UUID> idsARemover = cultivosRemovidos.stream()
                .map(TipoCultivo::getId)
                .filter(cultivoId -> !actividadRepository.existeOtraActividadVigenteConCultivo(establecimiento.getId(), cultivoId, idActividadActual))
                .toList();

        establecimiento.getTiposCultivos().removeIf(cultivo -> idsARemover.contains(cultivo.getId()));
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
                .orElseThrow(() -> new ResourceNotFoundException("No hay ninguna actividad vigente con ID: " + idActividad ));
    }


    //US-RESE-01: Reservar actividad - información sobre la actividad para reservarla
    /**
     * Arma la información necesaria para que un usuario pueda reservar una actividad: los días de actividad
     * disponibles, los rangos etarios activos con su precio, y los datos personales del usuario precargados
     * para el formulario de reserva. Verifica previamente que la actividad esté publicada y el
     * establecimiento no esté suspendido.
     *
     * @param idActividad ID de la actividad para la cual se quiere reservar
     * @param emailUsuario email del usuario autenticado que va a reservar
     * @return DTO con los días disponibles, rangos etarios, datos del usuario y días mínimos para reembolso
     * @throws UsuarioNotFound si no existe un usuario activo con ese email
     * @throws ActividadNotFoundException si no existe una actividad con ese ID
     * @throws ActividadNotActiveException si la actividad no está publicada o el establecimiento está suspendido
     */
    @Transactional
    public InfoParaReservarDTO getInfoParaReservar(UUID idActividad, String emailUsuario){
        LocalDateTime fhActual = LocalDateTime.now();

        // Gettear al usuario y visitante
        Usuario usuario = usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        // Gettear la actividad
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(ActividadNotFoundException::new);

        // Que la actividad esté disponible (estado Publicado) y el establecimiento activo
        if (actividad.getFechaHoraBaja() != null
                || actividad.getEstado().getNombre() != EstadoActividadNombre.PUBLICADO
                || actividad.getEstablecimiento().getEstadoActual()
                .getEstadoEstablecimiento()
                .getNombre().equals(EstadoEstablecimientoNombre.SUSPENDIDO))
            throw new ActividadNotActiveException();

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

        // Info del usuario a DTO
        UsuarioPreviewReservaDTO usuarioDTO = UsuarioPreviewReservaDTO.of(
                usuario.getNombre(),
                usuario.getFechaNacimiento(),
                usuario.getTipoIdentificacion().getNombre().name(),
                usuario.getIdentificacion()
        );

        //Armar el DTO principal y devolver
        return InfoParaReservarDTO.of(
                actividad,
                diaActividadReservaDTOList,
                usuarioDTO,
                rangoEtarioReservaDTOList,
                parametrosService.getInstance().getDiasMinReembolso());
    }

    void validarEstablecimientoNoSuspendido(UUID establecimientoId) {
        if (this.establecimientoRepository.establecimientoSuspendido(establecimientoId)) {
            throw new AppException(EstablecimientoError.ESTABLECIMIENTO_SUSPENDIDO);
        }

    }
    private void cancelarDiasFuturos(UUID idActividad, LocalDateTime ahora) {
        EstadoActividadDia cancelada = estadoActividadDiaRepository
                .findByNombre(EstadoActividadDiaNombre.CANCELADA)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el registro del estado CANCELADA de ActividadDia en la base de datos."));

        for (ActividadDia dia : actividadRepository.findDiasFuturosVigentes(idActividad, ahora)) {
            dia.cambiarEstado(cancelada, ahora, "Baja de la actividad");
            dia.setFechaHoraBaja(ahora);
        }
    }
    private Map<UUID, DTOCuposPorDia> obtenerCuposPorDia(List<ActividadDia> dias) {
        if (dias.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = dias.stream().map(ActividadDia::getId).toList();
        return reservaRepository.contarCuposPorDia(ids).stream()
                .collect(Collectors.toMap(DTOCuposPorDia::getActividadDiaId, c -> c));
    }

    private void sincronizarFotos(List<DTOActividadFotoReq> fotos, Actividad actividad) {
        List<DTOActividadFotoReq> pedidas = fotos == null ? List.of() : fotos;

        List<String> keyPedidas = pedidas.stream().map(DTOActividadFotoReq::getKey).toList();
        if (Set.copyOf(keyPedidas).size() != keyPedidas.size()) {
            throw new DatoInvalidoException("La actividad no puede repetir la misma foto");
        }

        actividad.getFotos().removeIf(foto -> !keyPedidas.contains(foto.getArchivo().getKey()));

        Map<String, ActividadFoto> conocidas = actividad.getFotos().stream()
                .collect(Collectors
                        .toMap(foto -> foto.getArchivo().getKey(), foto -> foto));

        for(int orden = 0; orden < pedidas.size(); orden++) {
            DTOActividadFotoReq pedida = pedidas.get(orden);
            ActividadFoto conocida = conocidas.get(pedida.getKey());

            if (conocida != null) {
                conocida.setOrden(orden);
                continue;
            }

            Archivo archivo = this.archivoService.reclamarArchivo(new ArchivoClaimRequest(pedida.getKey(), pedida.getNombre()), CarpetaArchivo.ACTIVIDADES);

            ActividadFoto nueva = new ActividadFoto();
            nueva.setArchivo(archivo);
            nueva.setOrden(orden);
            actividad.getFotos().add(nueva);
        }
    }
}





