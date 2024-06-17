package com.musigma.models.exception;

/**
 * La classe StockException représente une erreur spécifique liée aux opérations sur les stocks.
 */
public class StockException extends Exception {

    /**
     * Constructeur de la classe StockError.
     *
     * @param message le message détaillant l'erreur
     */
    public StockException(String message) {
        super(message);
    }
}