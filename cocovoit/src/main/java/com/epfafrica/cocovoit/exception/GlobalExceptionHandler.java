package com.epfafrica.cocovoit.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - validation Bean Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest req) {
        Map<String, String> erreurs = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            erreurs.put(fe.getField(), fe.getDefaultMessage());
        }
        ErreurResponse body = new ErreurResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Erreur de validation des champs",
                req.getRequestURI(),
                erreurs
        );
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - requete metier invalide
    @ExceptionHandler(RequeteInvalideException.class)
    public ResponseEntity<ErreurResponse> handleRequeteInvalide(RequeteInvalideException ex,
                                                               HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // 401 - mauvais identifiants
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErreurResponse> handleBadCredentials(BadCredentialsException ex,
                                                              HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Identifiants invalides", req);
    }

    // 403 - acces interdit (ownership / role)
    @ExceptionHandler(AccesInterditException.class)
    public ResponseEntity<ErreurResponse> handleAccesInterdit(AccesInterditException ex,
                                                             HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErreurResponse> handleSpringAccessDenied(AccessDeniedException ex,
                                                                  HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Acces refuse : droits insuffisants", req);
    }

    // 404 - ressource introuvable
    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurResponse> handleIntrouvable(RessourceIntrouvableException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    // 409 - conflit metier
    @ExceptionHandler(ConflitMetierException.class)
    public ResponseEntity<ErreurResponse> handleConflit(ConflitMetierException ex,
                                                        HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // 500 - filet de securite
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne : " + ex.getMessage(), req);
    }

    private ResponseEntity<ErreurResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        ErreurResponse body = new ErreurResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
