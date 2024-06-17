package com.musigma.models.exception;

/**
 * La classe ArtisteException représente une erreur spécifique liée aux opérations sur les artistes.
 */
public class ArtisteException extends Exception {

    /**
     * Constructeur de la classe ArtisteException.
     *
     * @param message le message détaillant l'erreur
     */
    public ArtisteException(String message) {
        super(message);
    }
}
