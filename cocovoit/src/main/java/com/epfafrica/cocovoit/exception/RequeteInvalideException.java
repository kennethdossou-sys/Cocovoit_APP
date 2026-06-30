package com.epfafrica.cocovoit.exception;

/** 400 - requete mal formee (validation metier) */
public class RequeteInvalideException extends RuntimeException {
    public RequeteInvalideException(String message) {
        super(message);
    }
}
