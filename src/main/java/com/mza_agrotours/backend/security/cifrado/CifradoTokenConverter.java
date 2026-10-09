package com.mza_agrotours.backend.security.cifrado;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * Cifra con {@link Cifrador} (AES-256-GCM) los atributos sensibles (tokens de terceros) antes de persistirlos.
 */
@Component
@Converter
public class CifradoTokenConverter implements AttributeConverter<String, String> {

    private final Cifrador cifrador;

    public CifradoTokenConverter(Cifrador cifrador) {
        this.cifrador = cifrador;
    }

    @Override
    public String convertToDatabaseColumn(String textoPlano) {
        return textoPlano == null ? null : cifrador.cifrar(textoPlano);
    }

    @Override
    public String convertToEntityAttribute(String valorDb) {
        return valorDb == null ? null : cifrador.descifrar(valorDb);
    }
}