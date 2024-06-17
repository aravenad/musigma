package com.musigma.modele.exception;

/**
 * La classe TypeTicketException représente une erreur spécifique liée aux opérations sur les types de tickets.
 */
public class TypeTicketException extends Exception {

    /**
     * Constructeur de la classe TypeTicketError.
     *
     * @param message le message détaillant l'erreur
     */
    public TypeTicketException(String message) {
        super(message);
    }
}