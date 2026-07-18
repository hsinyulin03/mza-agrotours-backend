package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.establecimiento.*;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.TipoCultivo;
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
    public void altaEstablecimiento(DTOEstablecimientoAlta dto) {
        validarCuitDisponible(dto.getCuit());

        Departamento departamento = obtenerDepartamento(dto.getDepartamentoId());
        List<TipoCultivo> cultivos = obtenerCultivos(dto.getCultivos());

        Establecimiento establecimiento = establecimientoMapper.dtoEstablecimientoAltaToEstablecimiento(dto);
        establecimiento.setDepartamento(departamento);
        establecimiento.setTiposCultivos(cultivos);
        EstablecimientoEstado estadoInicial = crearEstadoInicial();
        establecimiento.getEstados().add(estadoInicial);
        establecimientoRepository.save(establecimiento);
    }
    // Obtener datos establecimiento (panel productor)
    public DTODatosEstablecimiento obtenerDatosEstablecimiento(UUID id) {
        Establecimiento establecimiento = obtenerEstablecimiento(id);
        return mapearADatosEstablecimiento(establecimiento);
    }
    //MODIFICAR
    // IDENTIDAD
    @Transactional
    public DTODatosEstablecimiento modificarDescripcion( UUID id, DTOEstablecimientoDescripcionUpd dto) throws Exception {
        // buscar establecimiento
        Establecimiento establecimiento = obtenerEstablecimiento(id);
        // actualizar únicamente la descripción
        establecimiento.setDescripcion(dto.getDescripcion());
        establecimientoRepository.save(establecimiento);
        return mapearADatosEstablecimiento(establecimiento);
    }
   // MODIFICAR CONTACTO
    @Transactional
    public DTODatosEstablecimiento modificarContacto(UUID id, DTOEstablecimientoContactoUpd dto) throws Exception {
        // buscar establecimiento
        Establecimiento establecimiento = obtenerEstablecimiento(id);
        establecimiento.setTelefono(dto.getTelefono());
        establecimiento.setEmail(dto.getEmail());
        establecimientoRepository.save(establecimiento);
        return mapearADatosEstablecimiento(establecimiento);
    }
    // MODIFICAR CULTIVOS
    @Transactional
    public DTODatosEstablecimiento actualizarCultivos(UUID id, DTOEstablecimientoCultivosUpd dto) {

        // Busca el establecimiento por id
        Establecimiento establecimiento = obtenerEstablecimiento(id);

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
        // TODO FALTA VALIDAR CULTIVOS ACTIVOS
        List<TipoCultivo> cultivos = tipoCultivoRepository.findAllById(cultivosIds);
        if (cultivos.size() != cultivosIds.size()) {
            throw new EntityNotFoundException("Uno o más tipos de cultivo no existen");
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
        return establecimientoRepository.findById(id)
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
    private Integer contarActividadesPublicadas(Establecimiento establecimiento) {
        return (int) establecimiento.getActividades().stream()
                .filter(actividad ->
                        actividad.getFechaHoraBaja() == null &&
                                actividad.getEstado().getNombre() == EstadoActividadNombre.PUBLICADO)
                .count();
    }
    private void validarQueNoPoseaActividadesPublicadas(Establecimiento establecimiento) {
        boolean tieneActividadesPublicadas = establecimiento.getActividades().stream()
                .anyMatch(actividad ->
                        actividad.getFechaHoraBaja() == null &&
                                actividad.getEstado().getNombre() == EstadoActividadNombre.PUBLICADO);

        if (tieneActividadesPublicadas) {
            throw new BusinessException("No se puede dar de baja el establecimiento porque posee actividades publicadas");
        }
    }

}
