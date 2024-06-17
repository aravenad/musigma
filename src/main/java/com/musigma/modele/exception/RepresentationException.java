package com.musigma.modele.exception;

/**
 * La classe ArtisteException représente une erreur spécifique liée aux opérations sur les artistes.
 */
public class RepresentationException extends Exception {

    /**
     * Constructeur de la classe ArtisteException.
     *
     * @param message le message détaillant l'erreur
     */
    public RepresentationException(String message) {
        super(message);
    }
}
