package com.mza_agrotours.backend.support;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Discriminador compartido por todos los fixtures: cada valor bajo restriccion de unicidad
 * lleva un sufijo distinto, asi varias entidades pueden convivir en un mismo test.
 */
final class Seq {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private Seq() {
    }

    static int next() {
        return COUNTER.incrementAndGet();
    }
}
