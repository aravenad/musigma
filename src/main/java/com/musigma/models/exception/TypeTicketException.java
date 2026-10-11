package com.musigma.models.exception;

/**
 * La classe TypeTicketException représente une erreur spécifique liée aux opérations sur les types de tickets.
 */
public class TypeTicketException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de la classe TypeTicketError.
     *
     * @param message le message détaillant l'erreur
     */
    public TypeTicketException(String message) {
        super(message);
    }
}