package com.sgm.SGMbackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions REST.
 * Retourne toujours le format uniforme : { timestamp, status, message, path }
 *
 * Codes HTTP gérés :
 * 404 → ResourceNotFoundException
 * 422 → BusinessRuleException
 * 400 → Erreurs de validation @Valid
 * 500 → Toute autre exception non prévue
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(
            BusinessRuleException ex, HttpServletRequest req) {
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .findFirst()
                .orElse("Données invalides");
        return buildError(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex, HttpServletRequest req) {
        // Log l'exception côté serveur sans l'exposer au client
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne — consulter les logs", req.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message, String path) {
        Map<String, Object> err = new HashMap<>();
        err.put("timestamp", LocalDateTime.now().toString());
        err.put("status", status.value());
        err.put("message", message);
        err.put("path", path);
        return ResponseEntity.status(status).body(err);
    }
}
