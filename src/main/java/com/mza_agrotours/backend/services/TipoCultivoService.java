package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.tipoCultivo.*;
import com.mza_agrotours.backend.entities.cultivo.Estacionalidad;
import com.mza_agrotours.backend.entities.cultivo.EstacionalidadMes;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import com.mza_agrotours.backend.enums.Mes;
import com.mza_agrotours.backend.exceptions.EntityAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.mappers.TipoCultivoMapper;
import com.mza_agrotours.backend.repositories.RecetaRepository;
import com.mza_agrotours.backend.repositories.TipoCultivo.EstacionalidadRepository;
import com.mza_agrotours.backend.repositories.TipoCultivo.TipoCultivoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TipoCultivoService {

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;

    @Autowired
    private EstacionalidadRepository estacionalidadRepository;
    @Autowired
    private TipoCultivoMapper tipoCultivoMapper;
    @Autowired
    private RecetaRepository recetaRepository;
    ////US-CULT-06 ABM tipo cultivo (AM)
    // ALTA TIPO DE CULTIVO
    @Transactional
    public DTOTipoCultivoEditarDetalle altaTipoCultivo(DTOTipoCultivoAM dto) {
        validarNombreDisponible(dto.getNombre());

        TipoCultivo tipoCultivo = new TipoCultivo();
        tipoCultivo.setNombre(dto.getNombre());
        tipoCultivo.setDescripcion(dto.getDescripcion());
        tipoCultivo.setBeneficios(dto.getBeneficios());
        tipoCultivo.setEstacionalidadMeses(construirEstacionalidadMeses(dto.getEstacionalidadPorMes()));
        // todo informacion nutricional
        TipoCultivo guardado = tipoCultivoRepository.save(tipoCultivo);

        return mapearADatos(guardado);
    }
    // OBTENER DATOS TIPO CULTIVO (para prellenar el formulario de editar)
    @Transactional
    public DTOTipoCultivoEditarDetalle obtenerDatosTipoCultivo(UUID id) {
        TipoCultivo tipoCultivo = obtenerTipoCultivo(id);
        return mapearADatos(tipoCultivo);
    }
    // MODIFICAR TIPO CULTIVO
    @Transactional
    public DTOTipoCultivoEditarDetalle modificarTipoCultivo(UUID id, DTOTipoCultivoAM dto) {
        TipoCultivo tipoCultivo = obtenerTipoCultivo(id);
        validarNombreDisponibleParaModificar(id, dto.getNombre());

        tipoCultivo.setNombre(dto.getNombre());
        tipoCultivo.setDescripcion(dto.getDescripcion());
        tipoCultivo.setBeneficios(dto.getBeneficios());

        actualizarEstacionalidad(tipoCultivo, dto.getEstacionalidadPorMes());

        TipoCultivo guardado = tipoCultivoRepository.save(tipoCultivo);
        return mapearADatos(guardado);
    }
    // CONSULTAR ESTACIONALIDADES (catálogo fijo, para poblar el selector del formulario de cultivos)
    public List<DTOEstacionalidad> consultarEstacionalidades() {
        List<Estacionalidad> estacionalidades = estacionalidadRepository.findAll();
        return tipoCultivoMapper.estacionalidadesToDto(estacionalidades);
    }
    //// US-CULT-05 Consultar tipos de cultivo
    public DTOCatalogoTipoCultivo consultarCatalogoTipoCultivo() {

        List<TipoCultivo> tiposCultivo = tipoCultivoRepository.findAllByFechaHoraBajaIsNull();

        List<DTOTipoCultivoListado> listado = tiposCultivo.stream()
                .map(this::mapearAListado)
                .toList();

        DTOCatalogoTipoCultivo catalogo = new DTOCatalogoTipoCultivo();
        catalogo.setTotalCultivos(listado.size());
        // todas no hay fechabaja para las recetas
        catalogo.setTotalRecetas((int) recetaRepository.count());
        catalogo.setCultivos(listado);

        return catalogo;
    }









    /**
     * METODOS AUXILIARES
     */
    // ALTA
    private void validarNombreDisponible(String nombre) {
        if (tipoCultivoRepository.existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(nombre)) {
            throw new EntityAlreadyExistsException("Ya existe un tipo de cultivo con este nombre");
        }
    }

    private List<EstacionalidadMes> construirEstacionalidadMeses(List<EstacionalidadNombre> estacionalidadPorMes) {
        // Lista donde se van guardando las EstacionalidadMes creadas
        List<EstacionalidadMes> resultado = new ArrayList<>();
        // Obtiene todos los meses del enum Mes en el orden en que fueron definidos
        Mes[] meses = Mes.values();

        // Valida que la cantidad de estacionalidades recibidas coincida
        // exactamente con la cantidad de meses del año.
        if (estacionalidadPorMes == null || estacionalidadPorMes.size() != meses.length) {
            throw new ValidacionNegocioException(
                    "Se esperaban " + meses.length + " estacionalidades (una por mes), se recibieron " + estacionalidadPorMes.size()
            );
        }
        // Recorre la lista de estacionalidades(reposo-cosecha-crecimiento) por mes recibida
        for (int i = 0; i < estacionalidadPorMes.size(); i++) {
            // Convierte el estado/fase recibido ej COSECHA
            // en la entidad Estacionalidad correspondiente de la base de datos
            Estacionalidad estacionalidad = obtenerEstacionalidadPorNombre(estacionalidadPorMes.get(i));
            // Crea una nueva estacionalidad mes
            EstacionalidadMes em = new EstacionalidadMes();
            //el mes correspondiente según la posición del recorrido ej i=0 ENERO
            em.setMes(meses[i]);
            // asigna la estacionalidad obtenida para ese mes
            em.setEstacionalidad(estacionalidad);
            // agrega la relación a la lista resultado
            resultado.add(em);
        }

        return resultado;
    }
    private Estacionalidad obtenerEstacionalidadPorNombre(EstacionalidadNombre nombre) {
        return estacionalidadRepository.findByNombre(nombre)
                .orElseThrow(() -> new ValidacionNegocioException("No se encuentra configurada la estacionalidad " + nombre));
    }
    // DETALLE / EDITAR
    private DTOTipoCultivoEditarDetalle mapearADatos(TipoCultivo tipoCultivo) {
        DTOTipoCultivoEditarDetalle dto = tipoCultivoMapper.tipoCultivoToDtoEditarDetalle(tipoCultivo);

        dto.setEstacionalidadPorMes(obtenerEstacionalidadPorMes(tipoCultivo));
        return dto;
    }

    private List<DTOEstacionalidadMes> obtenerEstacionalidadPorMes(TipoCultivo tipoCultivo) {
        // Mapa mes -> EstacionalidadMes, para no depender del orden de la colección
        Map<Mes, EstacionalidadMes> porMes = tipoCultivo.getEstacionalidadMeses().stream()
                .collect(Collectors.toMap(EstacionalidadMes::getMes, em -> em));

        // Recorre los 12 meses en orden fijo y arma el DTO con mes + nombre
        return Arrays.stream(Mes.values())
                .map(mes -> {
                    // Obtiene la estacionalidad correspondiente al mes actual.
                    EstacionalidadMes em = porMes.get(mes);
                    // Si no existe una estacionalidad para ese mes
                    // el cultivo está inconsistente y se lanza la excepción
                    if (em == null) {
                        throw new IllegalStateException(
                                "El tipo de cultivo '" + tipoCultivo.getNombre()
                                        + "' no tiene estacionalidad cargada para el mes " + mes
                        );
                    }
                    // Crea el DTO que se envia al frontend.
                    DTOEstacionalidadMes dtoMes = new DTOEstacionalidadMes();
                    // Asigna el mes correspondiente
                    dtoMes.setMes(mes);
                    // Asigna el nombre de la estacionalidad
                    // (COSECHA, CRECIMIENTO o REPOSO).
                    dtoMes.setNombre(em.getEstacionalidad().getNombre());
                    return dtoMes;
                })
                .toList();
    }
    private TipoCultivo obtenerTipoCultivo(UUID id) {
        return tipoCultivoRepository.findByIdAndFechaHoraBajaIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encuentra el tipo de cultivo indicado"));
    }
    // MODIFICAR
    private void actualizarEstacionalidad(TipoCultivo tipoCultivo, List<EstacionalidadNombre> estacionalidadPorMes) {
        //Obtiene la lista de estacionalidades actualmente asociadas al cultivo
        List<EstacionalidadMes> estacionalidadActual = tipoCultivo.getEstacionalidadMeses();
        // Construye una nueva lista de estacionalidadMes a partir de los estados
        // recibidos (uno por cada mes del año).
        List<EstacionalidadMes> estacionalidadNueva = construirEstacionalidadMeses(estacionalidadPorMes);
        // Elimina todas las relaciones de estacionalidad actuales del cultivo.
        estacionalidadActual.clear();
        // Agrega las nuevas relaciones de estacionalidad a la lista
        estacionalidadActual.addAll(estacionalidadNueva);
    }

    private void validarNombreDisponibleParaModificar(UUID id, String nombre) {
        tipoCultivoRepository.findByNombreIgnoreCaseAndFechaHoraBajaIsNull(nombre)
                // Si el nombre pertenece a otro cultivo distinto del que se mandó a editar
                // se considera un nombre duplicado
                // el filter lo deja pasar y lanza la exception
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new EntityAlreadyExistsException("Ya existe un tipo de cultivo con ese nombre");
                });
    }
    // CATALOGO (listado admin)
    private DTOTipoCultivoListado mapearAListado(TipoCultivo tipoCultivo) {
        DTOTipoCultivoListado dto = tipoCultivoMapper.tipoCultivoToDtoListado(tipoCultivo);

        Integer cantidadRecetas = tipoCultivo.getRecetas().size();
        // TODO CONTAR ACTIVADADES POR CULTIVO
        Integer cantidadActividades = 0;
        dto.setCalendarioEstacionalidad(obtenerEstacionalidadPorMes(tipoCultivo));
        dto.setResumenCosecha(calcularResumenCosecha(tipoCultivo));
        dto.setCantidadRecetas(cantidadRecetas);
        dto.setCantidadActividades(cantidadActividades);
        dto.setPuedeEliminarse(cantidadRecetas == 0 && cantidadActividades == 0 );

        return dto;
    }
    /**
     * Genera un resumen de los meses de cosecha de un cultivo
     * Ej "Ene–Mar, Jun–Ago"
     */

    private String calcularResumenCosecha(TipoCultivo tipoCultivo) {
        //1.obtener únicamente los meses con estacionalidad COSECHA
        List<Mes> mesesEnCosecha = tipoCultivo.getEstacionalidadMeses().stream()
                //se queda solamente con los que son COSECHA.
                .filter(em -> em.getEstacionalidad().getNombre() == EstacionalidadNombre.COSECHA)
                // Los ordena por el número del mes
                //ordinal() devuelve la posición del enum
                .sorted(Comparator.comparingInt(em -> em.getMes().ordinal()))
                // pasa de estacionalidadmes a solo mes
                .map(EstacionalidadMes::getMes)
                .toList();
        // Si el cultivo nunca está en cosecha devuelve null igual no puede ser o si ?
        if (mesesEnCosecha.isEmpty()) {
            return null;
        }
        // 2. Este metodo forma los intervalos de cosecha
        // ej enero, febrero,abril...junio  a ene-feb abr-jun
        List<String> tramos = agruparEnTramosContiguos(mesesEnCosecha);
        // 3.Une los tramos en un solo string
        return String.join(", ", tramos);
    }

    private List<String> agruparEnTramosContiguos(List<Mes> mesesOrdenados) {
        List<String> tramos = new ArrayList<>();
        // indice donde comienza el tramo actual
        int inicioTramo = 0;
        // Un tramo termina cuando
        //  Se llega al final de la lista o
        //  El siguiente mes deja de ser consecutivo
        for (int i = 1; i <= mesesOrdenados.size(); i++) {
            //.ordinal metodo heredo del enum
            boolean esFinDeTramo = i == mesesOrdenados.size()
                    || mesesOrdenados.get(i).ordinal() != mesesOrdenados.get(i - 1).ordinal() + 1;

            if (esFinDeTramo) {
                Mes desde = mesesOrdenados.get(inicioTramo);
                Mes hasta = mesesOrdenados.get(i - 1);
                // Agrega el tramo formateado al resultado
                tramos.add(formatearTramo(desde, hasta));
                //El próximo tramo comienza en la posición actual
                inicioTramo = i;
            }
        }

        return tramos;
    }

    private String formatearTramo(Mes desde, Mes hasta) {
        if (desde == hasta) {
            return abreviarMes(desde);
        }
        return abreviarMes(desde) + "–" + abreviarMes(hasta);
    }

    private String abreviarMes(Mes mes) {
        return mes.getNombre().substring(0, 3);
    }




}
