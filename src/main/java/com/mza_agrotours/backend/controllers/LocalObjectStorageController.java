package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.services.LocalObjectStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * CRUD sobre los objetos guardados en disco. Solo existe cuando el proveedor
 * activo es el local: con S3 los bytes nunca pasan por el backend.
 */
@RestController
@RequestMapping("/object-storage/objects")
@ConditionalOnProperty(name = "object-storage.provider", havingValue = "local")
public class LocalObjectStorageController {
    private final LocalObjectStorageService localObjectStorageService;

    public LocalObjectStorageController(LocalObjectStorageService localObjectStorageService) {
        this.localObjectStorageService = localObjectStorageService;
    }

    @PutMapping("/{key}")
    public ResponseEntity<?> upload(@PathVariable String key, HttpServletRequest request) throws IOException {
        rejectFormContentType(request);
        this.localObjectStorageService.store(key, request.getInputStream(), request.getContentLengthLong());
        return ResponseEntity.ok(ApiResponse.ok(key));
    }

    /**
     * Spring registra FormContentFilter, que ante un PUT con content type de
     * formulario consume el body para exponerlo como parametros. Si dejaramos
     * pasar esos requests guardariamos un archivo vacio sin avisar.
     */
    private void rejectFormContentType(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null
                && contentType.toLowerCase().startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            throw new DatoInvalidoException("Content-Type no soportado para subir un objeto: " + contentType
                    + ". Usa el content type real del archivo o application/octet-stream");
        }
    }

    /**
     * Devuelve los bytes crudos, sin envolver en {@link ApiResponse}, para que
     * un <img src> pueda apuntar directamente aca.
     */
    @GetMapping("/{key}")
    public ResponseEntity<Resource> download(@PathVariable String key) {
        Resource resource = this.localObjectStorageService.load(key);
        return ResponseEntity.ok()
                .contentType(this.localObjectStorageService.contentTypeOf(key))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + key + "\"")
                .body(resource);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<?> delete(@PathVariable String key) {
        this.localObjectStorageService.delete(key);
        return ResponseEntity.ok(ApiResponse.ok(key));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(ApiResponse.ok(this.localObjectStorageService.list()));
    }
}