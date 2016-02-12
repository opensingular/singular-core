package br.net.mirante.singular.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface IFunction<T, R> extends Function<T, R>, Serializable {

}