package com.musigma.utils.exceptionMethods;

/**
 * Interface fonctionnelle représentant une opération qui accepte un argument
 * et peut lancer une exception.
 *
 * @param <T> le type de l'argument de l'opération
 */
@FunctionalInterface
public interface Setter<T> {

    /**
     * Effectue cette opération sur l'argument donné.
     *
     * @param t l'argument d'entrée
     * @throws Exception si une exception se produit lors de l'exécution de l'opération
     */
    void accept(T t) throws Exception;
}
