package com.musigma.modele.exception;

/**
 * La classe FestivalException représente une erreur spécifique liée aux opérations sur les festivals.
 */
public class FestivalException extends Exception {
    /**
     * Constructeur de la classe FestivalException.
     *
     * @param message le message détaillant l'erreur
     */
    public FestivalException(String message) {
        super(message);
    }
}
