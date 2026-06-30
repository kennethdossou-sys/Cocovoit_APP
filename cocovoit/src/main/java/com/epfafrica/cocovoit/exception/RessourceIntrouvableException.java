package com.epfafrica.cocovoit.exception;

/** 404 - ressource inexistante */
public class RessourceIntrouvableException extends RuntimeException {
    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
