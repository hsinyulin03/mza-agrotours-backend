package com.mza_agrotours.backend.utils;

import java.math.BigDecimal;
import java.util.List;

public final class CuartilesHelper {

    private CuartilesHelper() {
    }

    // Devuelve [Q1, Q2, Q3]
    public static List<BigDecimal> cuartiles(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            throw new IllegalArgumentException("No se pueden calcular cuartiles de una lista vacía");
        }
        List<BigDecimal> ordenados = valores.stream().sorted().toList();
        return List.of(
                percentil(ordenados, 0.25),
                percentil(ordenados, 0.50),
                percentil(ordenados, 0.75));
    }

    // Interpolación lineal, igual que percentile_cont de Postgres
    private static BigDecimal percentil(List<BigDecimal> ordenados, double p) {
        double posicion = p * (ordenados.size() - 1);
        int inferior = (int) Math.floor(posicion);
        int superior = (int) Math.ceil(posicion);
        BigDecimal fraccion = BigDecimal.valueOf(posicion - inferior);
        BigDecimal valorInferior = ordenados.get(inferior);
        BigDecimal valorSuperior = ordenados.get(superior);
        return valorInferior.add(valorSuperior.subtract(valorInferior).multiply(fraccion));
    }
}
