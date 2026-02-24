package com.sgm.SGMbackend.exception;

/**
 * Exception levée quand une ressource demandée n'existe pas en base.
 * Traduite en HTTP 404 par le GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
