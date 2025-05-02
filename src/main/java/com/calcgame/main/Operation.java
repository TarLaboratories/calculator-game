package com.calcgame.main;

import org.python.core.PyComplex;

import java.util.function.BiFunction;

public record Operation(int priority, BiFunction<PyComplex, PyComplex, PyComplex> f) {
}
