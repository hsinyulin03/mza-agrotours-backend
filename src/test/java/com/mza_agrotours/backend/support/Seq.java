package com.mza_agrotours.backend.support;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Discriminador compartido por fixtures y tests: cada valor bajo restriccion de unicidad
 * lleva un sufijo distinto, asi varias entidades pueden convivir en un mismo test.
 */
public final class Seq {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private Seq() {
    }

    public static int next() {
        return COUNTER.incrementAndGet();
    }
}
