package com.musigma.models.exception;

/**
 * La classe ArtisteException représente une erreur spécifique liée aux opérations sur les artistes.
 */
public class RepresentationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de la classe ArtisteException.
     *
     * @param message le message détaillant l'erreur
     */
    public RepresentationException(String message) {
        super(message);
    }
}
