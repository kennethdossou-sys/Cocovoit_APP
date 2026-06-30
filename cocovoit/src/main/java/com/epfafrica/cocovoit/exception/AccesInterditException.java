package com.epfafrica.cocovoit.exception;

/** 403 - connecte mais sans le droit (ownership) */
public class AccesInterditException extends RuntimeException {
    public AccesInterditException(String message){
        super(message);
    }
    
}
