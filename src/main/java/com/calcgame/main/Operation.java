package com.calcgame.main;

import org.python.core.PyComplex;

import java.util.function.BiFunction;

/**
 * Represents an operation used when parsing and evaluating a mathematical expression
 * @param priority the priority of the operation (more means it will be executed first)
 * @param f the function to use to evaluate
 */
public record Operation(int priority, BiFunction<PyComplex, PyComplex, PyComplex> f) {
}
