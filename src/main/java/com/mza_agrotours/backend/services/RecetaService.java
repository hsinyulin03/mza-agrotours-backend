package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.receta.DTORecetaAM;
import com.mza_agrotours.backend.dtos.receta.DTORecetaAMResponse;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.entities.receta.Duracion;
import com.mza_agrotours.backend.entities.receta.Ingrediente;
import com.mza_agrotours.backend.entities.receta.Paso;
import com.mza_agrotours.backend.entities.receta.Receta;
import com.mza_agrotours.backend.enums.DuracionNombre;
import com.mza_agrotours.backend.exceptions.EntityAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.repositories.TipoCultivo.TipoCultivoRepository;
import com.mza_agrotours.backend.repositories.receta.DuracionRepository;
import com.mza_agrotours.backend.repositories.receta.RecetaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


}
