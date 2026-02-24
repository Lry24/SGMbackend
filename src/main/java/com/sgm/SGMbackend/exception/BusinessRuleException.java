package com.sgm.SGMbackend.exception;

/**
 * Exception levée quand une règle métier est violée.
 * Traduite en HTTP 422 (Unprocessable Entity) par le GlobalExceptionHandler.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
