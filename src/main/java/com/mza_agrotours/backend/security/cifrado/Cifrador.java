package com.mza_agrotours.backend.security.cifrado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado autenticado AES-256-GCM: además de ocultar el contenido, garantiza que no fue alterado
 * (descifrar un valor modificado o generado con otra clave falla).
 * <p></p>
 * Formato: Base64 URL-safe sin padding de (IV de 12 bytes || texto cifrado + tag de 16 bytes),
 * apto para guardar en la base o viajar en una URL.
 * La clave se lee de {@code cifrado.tokens.clave} (32 bytes en Base64).
 */
@Component
public class Cifrador {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int LONGITUD_IV = 12;
    private static final int LONGITUD_TAG_BITS = 128;

    private final SecretKey clave;
    private final SecureRandom random = new SecureRandom();

    public Cifrador(@Value("${cifrado.tokens.clave}") String claveBase64) {
        byte[] bytesClave = Base64.getDecoder().decode(claveBase64);
        if (bytesClave.length != 32)
            throw new IllegalStateException("cifrado.tokens.clave debe ser de 32 bytes codificados en Base64");
        this.clave = new SecretKeySpec(bytesClave, "AES");
    }

    public String cifrar(String textoPlano) {
        try {
            byte[] iv = new byte[LONGITUD_IV];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(LONGITUD_TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array()
            );
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar el valor", e);
        }
    }

    /**
     * @throws ValorCifradoInvalidoException si el valor no tiene el formato esperado, fue alterado
     *                                       o se cifró con otra clave
     */
    public String descifrar(String valorCifrado) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(Base64.getUrlDecoder().decode(valorCifrado));
            if (buffer.remaining() <= LONGITUD_IV)
                throw new ValorCifradoInvalidoException();

            byte[] iv = new byte[LONGITUD_IV];
            buffer.get(iv);
            byte[] cifrado = new byte[buffer.remaining()];
            buffer.get(cifrado);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(LONGITUD_TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | AEADBadTagException e) {
            throw new ValorCifradoInvalidoException(e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo descifrar el valor", e);
        }
    }

    public static class ValorCifradoInvalidoException extends RuntimeException {
        public ValorCifradoInvalidoException() {
            super("El valor cifrado es inválido o fue alterado");
        }

        public ValorCifradoInvalidoException(Throwable causa) {
            super("El valor cifrado es inválido o fue alterado", causa);
        }
    }
}