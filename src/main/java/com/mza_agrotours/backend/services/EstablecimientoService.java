package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.establecimiento.*;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.EstablecimientoEstado;
import com.mza_agrotours.backend.entities.establecimiento.EstadoEstablecimiento;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.exceptions.EntityAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.mappers.EstablecimientoMapper;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoEstadoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.EstadoEstablecimientoRepository;
import com.mza_agrotours.backend.repositories.TipoCultivo.TipoCultivoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EstablecimientoService  {
    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private EstablecimientoEstadoRepository establecimientoEstadoRepository;

    @Autowired
    private EstadoEstablecimientoRepository estadoEstablecimientoRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

//    @Autowired
//    private ProductorRepository productorRepository;
    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;
    @Autowired
    private EstablecimientoMapper establecimientoMapper;
// ALTA ESTABLECIMIENTO
    @Transactional
    public DTODatosEstablecimiento altaEstablecimiento(DTOEstablecimientoAlta dto){
        validarCuitDisponible(dto.getCuit());

        Departamento departamento = obtenerDepartamento(dto.getDepartamentoId());
        List<TipoCultivo> cultivos = obtenerCultivos(dto.getCultivos());

        Establecimiento establecimiento = establecimientoMapper.dtoEstablecimientoAltaToEstablecimiento(dto);
        establecimiento.setDepartamento(departamento);
        establecimiento.setTiposCultivos(cultivos);

        establecimiento = crearEstablecimiento(establecimiento, "Alta de establecimiento");

        return mapearADatosEstablecimiento(establecimiento);
    }

    /**
     * Completa un Establecimiento recien construido (aun sin persistir) con su estado inicial
     * ACTIVO y lo guarda. Es el unico punto de nacimiento de un Establecimiento.
     */
    @Transactional
    public Establecimiento crearEstablecimiento(Establecimiento nuevoEstablecimiento, String motivoAlta) {
        EstablecimientoEstado estadoInicial = crearEstadoInicial(motivoAlta);
        nuevoEstablecimiento.getEstados().add(estadoInicial);
        nuevoEstablecimiento.setEstadoActual(estadoInicial);

        return establecimientoRepository.save(nuevoEstablecimiento);
    }

    // Obtener datos establecimiento (panel productor)
    public DTODatosEstablecimiento obtenerDatosEstablecimiento(UUID id) {
        Establecimiento establecimiento = obtenerEstablecimiento(id);
        return mapearADatosEstablecimiento(establecimiento);
    }
    @Transactional
    public DTOUpdEstablecimientoResponse modificarEstablecimiento(UUID id, DTODatosEstablecimientoUpd dto) {
        Establecimiento establecimiento = obtenerEstablecimiento(id);

        establecimiento.setDescripcion(dto.getDescripcion());
        establecimiento.setTelefono(dto.getTelefono());
        establecimiento.setEmail(dto.getEmail());
        establecimiento.setCvu(dto.getCvu());
        Establecimiento guardado = establecimientoRepository.save(establecimiento);

        DTOUpdEstablecimientoResponse response = new DTOUpdEstablecimientoResponse();
        response.setMensaje("Cambios guardados exitosamente.");
        response.setDatosEstablecimiento(mapearADatosEstablecimiento(guardado));
        return response;

    }
    /*// BAJA ESTABLECIMIENTO
    @Transactional
    public void bajaEstablecimiento(UUID id) {

        Establecimiento establecimiento = obtenerEstablecimiento(id);

        validarQueNoPoseaActividadesPublicadas(establecimiento);
        establecimiento.setFechaHoraBaja(LocalDateTime.now());

        establecimientoRepository.save(establecimiento);
    }
    // CONSULTAR ESTABLECIMIENTOS (listado de visitantes)
    public List<DTOCatalogoEstablecimientoVisitante> consultarEstablecimientosVisitantes() {
        // buscar establecimientos
        List<Establecimiento> establecimientos = establecimientoRepository.obtenerEstablecimientosActivos();
        return establecimientos.stream()
                .map(establecimiento -> {
                    DTOCatalogoEstablecimientoVisitante dto = establecimientoMapper.establecimientoToDtoConsultarEstableciminetoS(establecimiento);
                    dto.setCultivos(obtenerNombresCultivosActivos(establecimiento));
                    dto.setCantidadActividades(contarActividadesPublicadas(establecimiento));

                    return dto;
                })
                .toList();
    }
    // DETALLE ESTABLECIMIENTO (vista pública / visitante)
    public DTODetalleEstablecimientoVisitantes obtenerDetalleEstablecimientoVisitante(UUID id) {
        Establecimiento establecimiento = obtenerEstablecimiento(id);
        return mapearADetalleVisitante(establecimiento);
    }*/



    /**
     * METODOS AUXILIARES
     */
    // ALTA ESTABLECIMIENTO
    private void validarCuitDisponible(String cuit) {
        if (cuit != null && establecimientoRepository.existsByCuit(cuit)) {
            throw new EntityAlreadyExistsException("Ya existe un establecimiento registrado con ese CUIT");
        }
    }

    private Departamento obtenerDepartamento(UUID departamentoId) {
        return departamentoRepository.findById(departamentoId)
                .orElseThrow(() -> new EntityNotFoundException("No se encuentra el departamento indicado"));
    }

    private List<TipoCultivo> obtenerCultivos(List<UUID> cultivosIds) {
        if (cultivosIds == null || cultivosIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<TipoCultivo> cultivos = tipoCultivoRepository.findActivosByIds(cultivosIds);

        if (cultivos.size() != cultivosIds.size()) {
            throw new EntityNotFoundException("Uno o más tipos de cultivo no existen o se encuentran dados de baja");
        }

        return cultivos;
    }

    private EstablecimientoEstado crearEstadoInicial(String motivo) {
        EstadoEstablecimiento estadoActivo = estadoEstablecimientoRepository
                .findByNombreAndFechaBajaIsNull(EstadoEstablecimientoNombre.ACTIVO)
                .orElseThrow(() -> new ValidacionNegocioException("No se encuentra configurado el estado ACTIVO"));

        EstablecimientoEstado estadoInicial = new EstablecimientoEstado();
        estadoInicial.setFechaInicio(LocalDateTime.now());
        estadoInicial.setFechaFin(null);
        estadoInicial.setMotivo(motivo);
        estadoInicial.setEstadoEstablecimiento(estadoActivo);
        return estadoInicial;
    }
    // OBTENER DATOS ESTABLECIMIENTO
    private Establecimiento obtenerEstablecimiento(UUID id) {
        return establecimientoRepository.findByIdAndFechaHoraBajaIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encuentra el establecimiento indicado"));
    }
    private DTODatosEstablecimiento mapearADatosEstablecimiento(Establecimiento establecimiento) {
        DTODatosEstablecimiento dto = establecimientoMapper.establecimientoToDtoDatosEstablecimiento(establecimiento);
        // Se obtienen los cultivos del establecimiento
        // y se verfica si tienen actividades activas
        dto.setCultivos(obtenerCultivosDelEstablecimiento(establecimiento));
        return dto;
    }

    private List<DTOCultivoEstablecimientoResponse> obtenerCultivosDelEstablecimiento(
            Establecimiento establecimiento) {
        Map<UUID, TipoCultivo> cultivosUnicos = new LinkedHashMap<>();

        establecimiento.getActividades().stream()
                .filter(this::esActividadPublicada)
                .flatMap(actividad -> actividad.getCultivos().stream())
                .filter(cultivo -> cultivo.getFechaHoraBaja() == null)
                .forEach(cultivo -> cultivosUnicos.putIfAbsent(cultivo.getId(), cultivo));

        return cultivosUnicos.values().stream()
                .map(c -> {
                    DTOCultivoEstablecimientoResponse dto = new DTOCultivoEstablecimientoResponse();
                    dto.setId(c.getId());
                    dto.setNombre(c.getNombre());
                    return dto;
                })
                .toList();
    }
    private List<String> obtenerNombresCultivosActivos(Establecimiento establecimiento) {
        return establecimiento.getTiposCultivos().stream()
                .filter(cultivo -> cultivo.getFechaHoraBaja() == null)
                .map(TipoCultivo::getNombre)
                .toList();
    }
    // ACTIVIDADES (compartido entre consulta, baja y detalle)
    private boolean esActividadPublicada(Actividad actividad) {
        return actividad.getFechaHoraBaja() == null
                && actividad.getEstado().getNombre() == EstadoActividadNombre.PUBLICADO;
    }

    private Integer contarActividadesPublicadas(Establecimiento establecimiento) {
        return (int) establecimiento.getActividades().stream()
                .filter(this::esActividadPublicada)
                .count();
    }

    private void validarQueNoPoseaActividadesPublicadas(Establecimiento establecimiento) {
        boolean tieneActividadesPublicadas = establecimiento.getActividades().stream()
                .anyMatch(this::esActividadPublicada);

        if (tieneActividadesPublicadas) {
            throw new ValidacionNegocioException("No se puede dar de baja el establecimiento porque posee actividades publicadas");
        }
    }
   /* // DETALLE ESTABLECIMIENTO
    private DTODetalleEstablecimientoVisitantes mapearADetalleVisitante(Establecimiento establecimiento) {
        DTODetalleEstablecimientoVisitantes dto =
                establecimientoMapper.establecimientoToDtoDetalleVisitantes(establecimiento);

        dto.setCultivos(obtenerNombresCultivosActivos(establecimiento));
        dto.setActividades(obtenerActividadesPublicadasDetalle(establecimiento));

        return dto;
    }

    private List<DTODetalleEstablecimientoActividad> obtenerActividadesPublicadasDetalle(Establecimiento establecimiento) {
        return establecimiento.getActividades().stream()
                .filter(this::esActividadPublicada)
                .map(this::mapearAActividadDetalle)
                .toList();
    }*/
/*
    private DTODetalleEstablecimientoActividad mapearAActividadDetalle(Actividad actividad) {
        DTODetalleEstablecimientoActividad dto = establecimientoMapper.actividadToDtoDetalle(actividad);

        // TODO: cultivos de la actividad
        List<DTOCultivoResponse> cultivosAsociados = actividad.getCultivos().stream()
                .map(c -> new DTOCultivoResponse(c.getId(), c.getNombre()))
                .collect(Collectors.toList());

        private BigDecimal obtenerPrecioBaseVigente(Actividad actividad) {

            if (actividad.getActividadRangoEtarios() == null) {
                return null;
            }

            return actividad.getActividadRangoEtarios().stream()
                    .filter(ActividadRangoEtario::isEsTarifaBase)
                    .filter(r -> (r.getFechaHoraBaja() == null))
                    .map(ActividadRangoEtario::getPrecio)
                    .findFirst()
                    .orElse(null);
        }

        // TODO: implementar cálculo real del promedio de Calificacion.puntuacion
       // dto.setPuntuacion(calcularCalificacionPromedio(actividad));

        return dto;
    }
*/

}
