package org.jboss.as.quickstarts.kitchensink.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Global exception handler for REST controllers
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * Handle validation exceptions from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("violations", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    /**
     * Handle constraint violation exceptions from manual validation
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : violations) {
            String path = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(path, message);
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("violations", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralExceptions(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
