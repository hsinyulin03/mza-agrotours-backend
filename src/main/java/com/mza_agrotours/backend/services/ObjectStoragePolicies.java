package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.enums.CarpetaArchivo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * La policy que abre a lectura anonima las carpetas publicas del bucket, y
 * nada mas que esas: solo {@code s3:GetObject}, asi que sigue sin poder
 * listarse ni escribirse sin credenciales.
 * <p>
 * La aplicacion no la aplica. El bucket es infraestructura y el backend no
 * tiene permiso para cambiarle la policy; el documento se pega en la consola
 * del proveedor o lo aplica el aprovisionamiento. Se genera aca para que salga
 * del mismo enum que decide que carpeta es publica y no se desincronice.
 */
public final class ObjectStoragePolicies {

    private ObjectStoragePolicies() {
    }

    /**
     * @return el documento, o vacio si no hay ninguna carpeta publica: una
     * policy sin statements no es un documento valido
     */
    public static Optional<String> lecturaPublica(String bucket) {
        List<CarpetaArchivo> publicas = CarpetaArchivo.publicas();
        if (publicas.isEmpty()) {
            return Optional.empty();
        }

        String recursos = publicas.stream()
                .map(carpeta -> "\"arn:aws:s3:::" + bucket + "/" + carpeta.getPrefijo() + "/*\"")
                .collect(Collectors.joining(", "));

        return Optional.of("""
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Sid": "LecturaPublicaDeCarpetasPublicas",
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": [%s]
                    }
                  ]
                }""".formatted(recursos));
    }
}
