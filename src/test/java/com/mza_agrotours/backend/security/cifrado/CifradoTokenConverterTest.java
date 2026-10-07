package com.mza_agrotours.backend.security.cifrado;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CifradoTokenConverterTest {

    private static String clave(int bytes, byte relleno) {
        byte[] b = new byte[bytes];
        java.util.Arrays.fill(b, relleno);
        return Base64.getEncoder().encodeToString(b);
    }

    private final Cifrador cifrador = new Cifrador(clave(32, (byte) 0));
    private final CifradoTokenConverter converter = new CifradoTokenConverter(cifrador);

    @Test
    void cifraYDescifraElMismoValor() {
        String token = "APP_USR-1234567890-abcdef";

        String cifrado = converter.convertToDatabaseColumn(token);

        assertNotEquals(token, cifrado);
        assertEquals(token, converter.convertToEntityAttribute(cifrado));
    }

    @Test
    void mismoValorGeneraCifradosDistintos() {
        assertNotEquals(cifrador.cifrar("TG-token"), cifrador.cifrar("TG-token"));
    }

    @Test
    void cifradoEsAptoParaUrl() {
        assertTrue(cifrador.cifrar("un valor cualquiera para el state").matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void nullSeMantieneNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void rechazaValorAlteradoOMalFormado() {
        String cifrado = cifrador.cifrar("valor");
        char primero = cifrado.charAt(0);
        String alterado = (primero == 'A' ? 'B' : 'A') + cifrado.substring(1);

        assertThrows(Cifrador.ValorCifradoInvalidoException.class, () -> cifrador.descifrar(alterado));
        assertThrows(Cifrador.ValorCifradoInvalidoException.class, () -> cifrador.descifrar("no es base64!"));
        assertThrows(Cifrador.ValorCifradoInvalidoException.class, () -> cifrador.descifrar("AAAA"));
    }

    @Test
    void rechazaValorCifradoConOtraClave() {
        Cifrador otro = new Cifrador(clave(32, (byte) 1));
        String cifrado = otro.cifrar("valor");

        assertThrows(Cifrador.ValorCifradoInvalidoException.class, () -> cifrador.descifrar(cifrado));
    }

    @Test
    void rechazaClaveDeLongitudInvalida() {
        assertThrows(IllegalStateException.class, () -> new Cifrador(clave(16, (byte) 0)));
    }
}