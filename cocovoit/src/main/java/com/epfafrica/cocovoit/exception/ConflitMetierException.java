package com.epfafrica.cocovoit.exception;

/** 409 - conflit metier (trajet complet, double reservation...) */
public class ConflitMetierException extends RuntimeException {
    public ConflitMetierException(String message) {
        super(message);
    }
}
