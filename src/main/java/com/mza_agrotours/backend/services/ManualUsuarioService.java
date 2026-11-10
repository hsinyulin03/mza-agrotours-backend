package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.dtos.archivo.DTOFotosResponse;
import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.entities.ManualUsuario;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.repositories.ManualUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManualUsuarioService {
    private final ManualUsuarioRepository manualUsuarioRepository;
    private final ArchivoService archivoService;

    public ManualUsuarioService(ManualUsuarioRepository manualUsuarioRepository, ArchivoService archivoService) {
        this.manualUsuarioRepository = manualUsuarioRepository;
        this.archivoService = archivoService;
    }

    /**
     * Reemplaza el manual vigente por el pdf que el admin ya subio al bucket.
     * Si es la misma key no se toca nada: reclamar una key ya asociada fallaria.
     */
    @Transactional
    public DTOFotosResponse actualizarManual(ArchivoClaimRequest pedido) {
        ManualUsuario manual = this.manualUsuarioRepository.findFirstByOrderByIdAsc()
                .orElseGet(ManualUsuario::new);

        Archivo actual = manual.getArchivo();
        if (actual == null || !actual.getKey().equals(pedido.getKey())) {
            manual.setArchivo(this.archivoService.reclamarArchivo(pedido, CarpetaArchivo.MANUALES));
        }

        ManualUsuario guardado = this.manualUsuarioRepository.save(manual);
        return this.archivoService.mapearFoto(guardado.getArchivo());
    }

    /**
     * @throws ResourceNotFoundException si todavia no se subio ningun manual
     */
    @Transactional(readOnly = true)
    public DTOFotosResponse obtenerManual() {
        return this.manualUsuarioRepository.findFirstByOrderByIdAsc()
                .map(ManualUsuario::getArchivo)
                .map(this.archivoService::mapearFoto)
                .orElseThrow(() -> new ResourceNotFoundException("Todavia no se cargo el manual de usuario"));
    }
}
