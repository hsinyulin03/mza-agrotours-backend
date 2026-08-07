package com.mza_agrotours.backend.services;
import com.mza_agrotours.backend.dtos.receta.*;
import com.mza_agrotours.backend.mappers.RectaMapper;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.entities.receta.Duracion;
import com.mza_agrotours.backend.entities.receta.Ingrediente;
import com.mza_agrotours.backend.entities.receta.Paso;
import com.mza_agrotours.backend.entities.receta.Receta;
import com.mza_agrotours.backend.exceptions.EntityAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.repositories.TipoCultivo.TipoCultivoRepository;
import com.mza_agrotours.backend.repositories.receta.DuracionRepository;
import com.mza_agrotours.backend.repositories.receta.RecetaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecetaService {
    @Autowired
    private RecetaRepository recetaRepository;

    @Autowired
    private DuracionRepository duracionRepository;

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;
    @Autowired
    private  RectaMapper recetaMapper;


    @Transactional
   public DTORecetaAMResponse altaReceta(DTORecetaAM dto) {

        //1.Se valida que no coincida el nombre
        validarNombreDisponible(dto.getNombre());
       // 2. Se valida que los cultivos existan
        List<TipoCultivo> cultivos = obtenerCultivos(dto.getCultivosIds());
        // 3.Se calcula la duracion en base al tiempoaprox
       Duracion duracion = obtenerDuracionSegunTiempo(dto.getTiempoMinsAprox());
        //4. Se construye la receta
       Receta receta = new Receta();
       receta.setNombre(dto.getNombre());
       receta.setDescripcion(dto.getDescripcion());
       receta.setPorciones(dto.getPorciones());
       receta.setTiempoMinsAprox(dto.getTiempoMinsAprox());
       receta.setDificultad(dto.getDificultad());
       receta.setDuracion(duracion);
       //ConstruirPaso genera para c/u una instacia de paso con el nro + descripcion
       receta.setPasos(construirPasos(dto.getPasos()));
       //Construiringrediente genera para c/u instacia de ingrediente
       receta.setIngredientes(construirIngredientes(dto.getIngredientes()));

       Receta guardada = recetaRepository.save(receta);
       // 5. Se asocia la receta a cada uno de los cultivos ingresados
        for (TipoCultivo cultivo : cultivos) {
            cultivo.getRecetas().add(receta);
        }
        tipoCultivoRepository.saveAll(cultivos);

        DTORecetaAMResponse response = new DTORecetaAMResponse();
        response.setIdReceta(guardada.getId());
        response.setMensaje("Se agregó la receta " + guardada.getNombre() + " al catálogo.");
        return response;
    }
    // OBTENER DATOS RECETA (para prellenar el formulario de editar)
    public DTORecetaDetalleM obtenerDatosReceta(UUID id) {
        Receta receta = obtenerReceta(id);
        DTORecetaDetalleM dto = recetaMapper.recetaToDtoDetalle(receta);

        // Arma dto cultivo (id y nombre )
        dto.setCultivos(obtenerCultivosDeReceta(receta.getId()));
        dto.setIngredientes(receta.getIngredientes().stream()
                // De cada Ingrediente obtiene únicamente el texto nombre+cantidad
                .map(Ingrediente::getNombreycantidad)
                .toList());
        dto.setPasos(receta.getPasos().stream()
                // Ordena los pasos según su número
                // p1 y p2  pasos que está comparando.
                //  obtiene el número de cada paso.
                // Integer.compare() hace que se ordenen de menor a mayor
                .sorted((p1, p2) -> Integer.compare(p1.getNumero(), p2.getNumero()))
                // De cada Paso obtiene únicamente la descripción.
                .map(Paso::getDescripcion)
                .toList());
        return dto;
    }
    // MODIFICAR RECETA
    @Transactional
    public DTORecetaAMResponse modificarReceta(UUID id, DTORecetaAM dto) {

        Receta receta = obtenerReceta(id);

        // 1. Se valida que el nombre no pertenezca a otra receta
        validarNombreDisponibleParaModificar(id, dto.getNombre());
        // 2. Se valida que los cultivos existan
        List<TipoCultivo> cultivosNuevos = obtenerCultivos(dto.getCultivosIds());
        // 3. Se recalcula la duración en base al tiempo aprox
        Duracion duracion = obtenerDuracionSegunTiempo(dto.getTiempoMinsAprox());

        // 4. Se actualizan los datos básicos
        receta.setNombre(dto.getNombre());
        receta.setDescripcion(dto.getDescripcion());
        receta.setPorciones(dto.getPorciones());
        receta.setTiempoMinsAprox(dto.getTiempoMinsAprox());
        receta.setDificultad(dto.getDificultad());
        receta.setDuracion(duracion);

        // 5. Se reemplazan pasos e ingredientes
        receta.getPasos().clear();
        receta.getPasos().addAll(construirPasos(dto.getPasos()));

        receta.getIngredientes().clear();
        receta.getIngredientes().addAll(construirIngredientes(dto.getIngredientes()));

        Receta guardada = recetaRepository.save(receta);

        // 6. Se resincroniza la relación con TipoCultivo (unidireccional, dueña en TipoCultivo)
        // Cultivos que actualmente tienen esta receta asociada
        List<TipoCultivo> cultivosActuales = tipoCultivoRepository.findByRecetasId(receta.getId());

        List<UUID> idsNuevos = cultivosNuevos.stream().map(TipoCultivo::getId).toList();
        List<UUID> idsActuales = cultivosActuales.stream().map(TipoCultivo::getId).toList();

        // Cultivos a los que hay que sacarles la receta (estaban antes ya no vinieron en el dto)
        List<TipoCultivo> aQuitar = cultivosActuales.stream()
        // Se queda solamente con los cultivos actuales que su id no este  en la lista de ids nuevos
                .filter(c -> !idsNuevos.contains(c.getId()))
                .toList();

        // Cultivos a los que hay que agregarles la receta (vinieron en el dtono la tenían antes)
        List<TipoCultivo> aAgregar = cultivosNuevos.stream()
                .filter(c -> !idsActuales.contains(c.getId()))
                .toList();

       // Recorre todos los cultivos que ya estaban asociados
        // pero que ahora deben quitarse de la receta.
        aQuitar.forEach(c -> c.getRecetas().remove(receta));
        // Recorre todos los cultivos que son nuevos para la receta
        // y agrega la receta a las recetas del cultivo
        aAgregar.forEach(c -> c.getRecetas().add(receta));



        List<TipoCultivo> afectados = new ArrayList<>();
        afectados.addAll(aQuitar);
        afectados.addAll(aAgregar);

        tipoCultivoRepository.saveAll(afectados);

        DTORecetaAMResponse response = new DTORecetaAMResponse();
        response.setIdReceta(guardada.getId());
        response.setMensaje("Se guardaron los cambios de la receta " + guardada.getNombre() + ".");
        return response;
    }
    // BAJA RECETA
    @Transactional
    public DTORectaBResponse bajaReceta(UUID id) {
        Receta receta = obtenerReceta(id);
        receta.setFechaHoraBaja(LocalDateTime.now());
        recetaRepository.save(receta);
        DTORectaBResponse response = new DTORectaBResponse();
        response.setIdReceta(receta.getId());
        response.setMensaje("Se eliminó la receta " + receta.getNombre() + " del catágolo de recetas.");
        return response;
    }








    /**
     * METODOS AUXILIARES
     */
    // AM
    private void validarNombreDisponible(String nombre) {
        if (recetaRepository.existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(nombre)) {
            throw new EntityAlreadyExistsException("Ya existe una receta con ese nombre");
        }
    }

    private Duracion obtenerDuracionSegunTiempo(Integer tiempoMinsAprox) {
        return duracionRepository.findByRangoDeMinutos(tiempoMinsAprox)
                .orElseThrow(() -> new ValidacionNegocioException(
                        "No se encuentra configurada una duración para " + tiempoMinsAprox + " minutos"
                ));
    }
    private List<Paso> construirPasos(List<String> pasos) {
        List<Paso> resultado = new ArrayList<>();
        for (int i = 0; i < pasos.size(); i++) {
            Paso paso = new Paso();
            paso.setNumero(i + 1);
            paso.setDescripcion(pasos.get(i));
            resultado.add(paso);
        }
        return resultado;
    }
    private List<Ingrediente> construirIngredientes(List<String> ingredientes) {

        List<Ingrediente> resultado = new ArrayList<>();

        for (String texto : ingredientes) {
            Ingrediente ing = new Ingrediente();
            ing.setNombreycantidad(texto);
            resultado.add(ing);
        }

        return resultado;
    }

    private List<TipoCultivo> obtenerCultivos(List<UUID> cultivosIds) {
        List<TipoCultivo> cultivos = tipoCultivoRepository.findAllById(cultivosIds);
        if (cultivos.size() != cultivosIds.size()) {
            throw new EntityNotFoundException("Uno o más cultivos no existen");
        }
        return cultivos;
    }
    private Receta obtenerReceta(UUID id) {
        return recetaRepository.findByIdAndFechaHoraBajaIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encuentra la receta indicada"));
    }
    private List<DTORecetaDetalleCultivoM> obtenerCultivosDeReceta(UUID recetaId) {
        return tipoCultivoRepository.findByRecetasId(recetaId).stream()
                .map(cultivo -> new DTORecetaDetalleCultivoM(
                        cultivo.getId(),
                        cultivo.getNombre()))
                .toList();
    }
    private void validarNombreDisponibleParaModificar(UUID id, String nombre) {
        recetaRepository.findByNombreIgnoreCaseAndFechaHoraBajaIsNull(nombre)
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new EntityAlreadyExistsException("Ya existe una receta con ese nombre");
                });
    }


}
