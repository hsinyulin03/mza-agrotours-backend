package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.establecimiento.*;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.TipoCultivo;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.EstadoEstablecimiento;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.EstablecimientoEstado;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.exceptions.BusinessException;
import com.mza_agrotours.backend.exceptions.EntityAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.mappers.EstablecimientoMapper;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import com.mza_agrotours.backend.repositories.EstadoEstablecimientoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoEstadoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.TipoCultivoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
        EstablecimientoEstado estadoInicial = crearEstadoInicial();
        establecimiento.getEstados().add(estadoInicial);
        establecimientoRepository.save(establecimiento);
        return mapearADatosEstablecimiento(establecimiento);
    }

    // Obtener datos establecimiento (panel productor)
    public DTODatosEstablecimiento obtenerDatosEstablecimiento(UUID id) {
        Establecimiento establecimiento = obtenerEstablecimiento(id);
        return mapearADatosEstablecimiento(establecimiento);
    }
    @Transactional
    public DTODatosEstablecimiento modificarEstablecimiento(UUID id, DTODatosEstablecimientoUpd dto) {
        Establecimiento establecimiento = obtenerEstablecimiento(id);

        establecimiento.setDescripcion(dto.getDescripcion());
        establecimiento.setTelefono(dto.getTelefono());
        establecimiento.setEmail(dto.getEmail());
        establecimiento.setCvu(dto.getCvu());

        // MODIFICAR CULTIVOS
        // Lista de cultivos que el establecimiento tiene ASIGNADOS actualmente en la base
        List<TipoCultivo> cultivosActuales = establecimiento.getTiposCultivos();

        // Busca en la base los TipoCultivo correspondientes a los ids que mandó el frontend.
        // obtenerCultivos ya valida que todos los ids existan
        List<TipoCultivo> cultivosNuevos = obtenerCultivos(dto.getCultivosIds());

        // Se pasan ambas listas a Set<UUID> para poder comparar por id
        Set<UUID> idsActuales = cultivosActuales.stream()
                .map(TipoCultivo::getId)
                .collect(Collectors.toSet());
        Set<UUID> idsNuevos = cultivosNuevos.stream()
                .map(TipoCultivo::getId)
                .collect(Collectors.toSet());

        // "A agregar" = cultivos que vinieron del frontend (cultivosNuevos)
        // y que el establecimiento TODAVÍA NO tiene (su id no está en idsActuales)
        List<TipoCultivo> cultivosAAgregar = cultivosNuevos.stream()
                .filter(c -> !idsActuales.contains(c.getId()))
                .toList();

        // "A eliminar" = cultivos que el establecimiento tiene actualmente (cultivosActuales)
        // pero que  NO vinieron en la lista del frontend (su id no está en idsNuevos)
        List<TipoCultivo> cultivosAEliminar = cultivosActuales.stream()
                .filter(c -> !idsNuevos.contains(c.getId()))
                .toList();

        // Se recorren los cultivos a eliminar y se sacan
        for (TipoCultivo cultivo : cultivosAEliminar) {
            // TODO: Validar si este cultivo posee actividades activas asociadas al establecimiento antes de eliminar la relación.
            cultivosActuales.remove(cultivo);
        }

        // Se agregan a la colección actual los cultivos nuevos que faltaban
        cultivosActuales.addAll(cultivosAAgregar);

        // Persiste los cambios de la relación establecimiento-cultivos
        establecimientoRepository.save(establecimiento);
        return mapearADatosEstablecimiento(establecimiento);
    }
    // BAJA ESTABLECIMIENTO
    @Transactional
    public void bajaEstablecimiento(UUID id) {

        Establecimiento establecimiento = obtenerEstablecimiento(id);

        validarQueNoPoseaActividadesPublicadas(establecimiento);
        establecimiento.setFechaHoraBaja(LocalDateTime.now());

        establecimientoRepository.save(establecimiento);
    }
    // CONSULTAR ESTABLECIMIENTOS (listado de visitantes)
    public List<DTOConsultarEstablecimientoSVisitante> consultarEstablecimientosVisitantes() {
        // buscar establecimientos
        List<Establecimiento> establecimientos = establecimientoRepository.obtenerEstablecimientosActivos();
        return establecimientos.stream()
                .map(establecimiento -> {
                    DTOConsultarEstablecimientoSVisitante dto = establecimientoMapper.establecimientoToDtoConsultarEstableciminetoS(establecimiento);
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
    }



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

    private EstablecimientoEstado crearEstadoInicial() {
        EstadoEstablecimiento estadoActivo = estadoEstablecimientoRepository
                .findByNombreAndFechaBajaIsNull(EstadoEstablecimientoNombre.ACTIVO)
                .orElseThrow(() -> new BusinessException("No se encuentra configurado el estado ACTIVO"));

        EstablecimientoEstado estadoInicial = new EstablecimientoEstado();
        estadoInicial.setFechaInicio(LocalDateTime.now());
        estadoInicial.setFechaFin(null);
        estadoInicial.setMotivo("Alta de establecimiento");
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

    private List<DTODatosEstablecimientoCultivos> obtenerCultivosDelEstablecimiento(
            Establecimiento establecimiento) {
        // TODO falta implementar validación cultivos del establecimiento con actividades activas
        return establecimiento.getTiposCultivos()
                .stream()
                .filter(c -> c.getFechaHoraBaja() == null)
                .map(c -> {
                    DTODatosEstablecimientoCultivos dto = new DTODatosEstablecimientoCultivos();
                    dto.setId(c.getId().toString());
                    dto.setNombre(c.getNombre());
                    dto.setTieneActividadesActivas(false);
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
            throw new BusinessException("No se puede dar de baja el establecimiento porque posee actividades publicadas");
        }
    }
    // DETALLE ESTABLECIMIENTO
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
    }

    private DTODetalleEstablecimientoActividad mapearAActividadDetalle(Actividad actividad) {
        DTODetalleEstablecimientoActividad dto = establecimientoMapper.actividadToDtoDetalle(actividad);

        // TODO: cultivos de la actividad
        dto.setCultivos(new ArrayList<>());

        // TODO: implementar cálculo real del precio vigente para el rango etario "Adulto"
        //dto.setPrecioDesde(obtenerPrecioAdulto(actividad));

        // TODO: implementar cálculo real del promedio de Calificacion.puntuacion
       // dto.setPuntuacion(calcularCalificacionPromedio(actividad));

        return dto;
    }


}
