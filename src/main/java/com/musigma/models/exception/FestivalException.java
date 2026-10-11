package com.musigma.models.exception;

/**
 * La classe FestivalException représente une erreur spécifique liée aux opérations sur les festivals.
 */
public class FestivalException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de la classe FestivalException.
     *
     * @param message le message détaillant l'erreur
     */
    public FestivalException(String message) {
        super(message);
    }
}
