package com.mza_agrotours.backend.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CuartilesHelperTest {

    @Test
    void conUnSoloValorLosTresCuartilesSonEseValor() {
        assertCuartiles(valores(100), "100", "100", "100");
    }

    @Test
    void conCantidadImparLosCuartilesCaenSobreValoresDeLaLista() {
        assertCuartiles(valores(10, 20, 30, 40, 50), "20", "30", "40");
    }

    @Test
    void conCantidadParInterpolaEntreLosValoresVecinos() {
        assertCuartiles(valores(10, 20, 30, 40), "17.5", "25", "32.5");
    }

    @Test
    void conValoresRepetidosPuedenCoincidirVariosCuartiles() {
        assertCuartiles(valores(100, 100, 100, 200), "100", "100", "125");
    }

    @Test
    void ordenaLosValoresAntesDeCalcular() {
        assertCuartiles(valores(50, 10, 40, 20, 30), "20", "30", "40");
    }

    @Test
    void conListaVaciaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> CuartilesHelper.cuartiles(List.of()));
    }

    private static List<BigDecimal> valores(int... numeros) {
        return Arrays.stream(numeros).mapToObj(BigDecimal::valueOf).toList();
    }

    private static void assertCuartiles(List<BigDecimal> valores, String q1, String q2, String q3) {
        List<BigDecimal> cuartiles = CuartilesHelper.cuartiles(valores);
        List<String> esperados = List.of(q1, q2, q3);
        for (int i = 0; i < 3; i++) {
            BigDecimal esperado = new BigDecimal(esperados.get(i));
            assertEquals(0, esperado.compareTo(cuartiles.get(i)),
                    "Q" + (i + 1) + ": esperado " + esperado + " pero fue " + cuartiles.get(i));
        }
    }
}
