package com.mza_agrotours.backend.services;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Genera y valida las keys de los objetos almacenados.
 * <p>
 * Las keys son planas y las genera el servidor: el cliente nunca elige donde
 * se guarda un archivo. Al no admitir separadores de ruta, una key valida no
 * puede escaparse del directorio de almacenamiento.
 */
public final class ObjectStorageKeys {
    private static final Pattern EXTENSION = Pattern.compile("^[A-Za-z0-9]{1,10}$");
    private static final Pattern KEY = Pattern.compile("^[A-Za-z0-9-]{36}(\\.[A-Za-z0-9]{1,10})?$");

    private ObjectStorageKeys() {
    }

    public static String generate(String filename) {
        String key = UUID.randomUUID().toString();
        String extension = extractExtension(filename);
        return extension == null ? key : key + "." + extension;
    }

    public static boolean isValid(String key) {
        return key != null && KEY.matcher(key).matches();
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