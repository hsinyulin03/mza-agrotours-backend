package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.enums.CarpetaArchivo;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Genera y valida las keys de los objetos almacenados.
 * <p>
 * Las genera el servidor: el cliente nunca elige donde se guarda un archivo ni
 * en que carpeta cae. El prefijo solo puede ser el de una {@link CarpetaArchivo}
 * conocida, asi que una key valida no puede apuntar a una ruta arbitraria del
 * bucket. El prefijo es opcional al validar porque hay keys planas guardadas
 * de antes de que existieran las carpetas.
 */
public final class ObjectStorageKeys {
    private static final Pattern EXTENSION = Pattern.compile("^[A-Za-z0-9]{1,10}$");
    private static final Pattern KEY = Pattern.compile(
            "^(" + prefijosConocidos() + ")?[A-Za-z0-9-]{36}(\\.[A-Za-z0-9]{1,10})?$");

    private ObjectStorageKeys() {
    }

    public static String generate(CarpetaArchivo carpeta, String filename) {
        String nombre = UUID.randomUUID().toString();
        String extension = extractExtension(filename);
        if (extension != null) {
            nombre = nombre + "." + extension;
        }
        return carpeta.getPrefijo() + "/" + nombre;
    }

    public static boolean isValid(String key) {
        return key != null && KEY.matcher(key).matches();
    }

    private static String prefijosConocidos() {
        return Arrays.stream(CarpetaArchivo.values())
                .map(carpeta -> Pattern.quote(carpeta.getPrefijo()) + "/")
                .collect(Collectors.joining("|"));
    }

    private static String extractExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        String extension = filename.substring(dot + 1);
        return EXTENSION.matcher(extension).matches() ? extension : null;
    }
}
