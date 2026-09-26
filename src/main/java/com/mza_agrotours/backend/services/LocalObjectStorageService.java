package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Almacenamiento de objetos sobre el disco local, solo para desarrollo.
 * Todas las operaciones pasan por {@link #resolve(String)}, que garantiza que
 * nunca se lea ni se escriba fuera de {@code object-storage.local-path}.
 */
@Service
@ConditionalOnProperty(name = "object-storage.provider", havingValue = "local")
public class LocalObjectStorageService {
    private static final int BUFFER_SIZE = 8192;

    private final Path root;
    private final long maxFileSize;

    public LocalObjectStorageService(
            @Value("${object-storage.local-path}") String localPath,
            @Value("${object-storage.max-file-size}") long maxFileSize) throws IOException {
        this.root = Paths.get(localPath).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        Files.createDirectories(this.root);
    }

    public void store(String key, InputStream content, long contentLength) throws IOException {
        if (contentLength > this.maxFileSize) {
            throw new DatoInvalidoException(tamanioExcedidoMsg());
        }

        Path path = resolve(key);
        try (OutputStream output = Files.newOutputStream(path)) {
            long written = copyWithLimit(content, output);
            if (contentLength >= 0 && written != contentLength) {
                throw new DatoInvalidoException("El cuerpo recibido (" + written
                        + " bytes) no coincide con el Content-Length declarado (" + contentLength + ")");
            }
        } catch (RuntimeException | IOException e) {
            // El archivo quedo a medio escribir: no dejamos basura en el disco.
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // Nada que hacer, se propaga el error original.
            }
            throw e;
        }
    }

    public Resource load(String key) {
        Path path = resolve(key);
        if (!Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("No existe el objeto " + key);
        }
        return new FileSystemResource(path);
    }

    public void delete(String key) {
        Path path = resolve(key);
        try {
            if (!Files.deleteIfExists(path)) {
                throw new ResourceNotFoundException("No existe el objeto " + key);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<String> list() {
        try (Stream<Path> files = Files.list(this.root)) {
            return files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * El content type sale de la extension de la key, que se conserva al
     * generarla, asi no hace falta persistir metadata aparte.
     */
    public MediaType contentTypeOf(String key) {
        return MediaTypeFactory.getMediaType(key).orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    private Path resolve(String key) {
        if (!ObjectStorageKeys.isValid(key)) {
            throw new DatoInvalidoException("Key de objeto invalida: " + key);
        }
        Path path = this.root.resolve(key).normalize();
        if (!path.startsWith(this.root)) {
            throw new DatoInvalidoException("Key de objeto invalida: " + key);
        }
        return path;
    }

    /**
     * Content-Length es opcional (por ejemplo en transferencias chunked), asi
     * que el limite tambien se aplica mientras se copia.
     */
    private long copyWithLimit(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > this.maxFileSize) {
                throw new DatoInvalidoException(tamanioExcedidoMsg());
            }
            output.write(buffer, 0, read);
        }
        return total;
    }

    private String tamanioExcedidoMsg() {
        return "El archivo supera el tamanio maximo permitido de " + this.maxFileSize + " bytes";
    }
}