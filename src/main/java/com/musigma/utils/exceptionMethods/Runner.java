package com.musigma.utils.exceptionMethods;

/**
 * Interface fonctionnelle représentant une opération qui peut lancer une exception.
 */
@FunctionalInterface
public interface Runner {

    /**
     * Exécute l'opération qui peut lancer une exception.
     *
     * @throws Exception si une exception se produit lors de l'exécution de l'opération
     */
    void run() throws Exception;
}
