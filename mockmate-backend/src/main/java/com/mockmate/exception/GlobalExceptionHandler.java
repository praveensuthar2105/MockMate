package com.mockmate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFound(NoResourceFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Bad Request");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        response.put("message", "Validation failed");
        response.put("details", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        if (ex.getMessage() != null && (ex.getMessage().toLowerCase().contains("already in use") || ex.getMessage().toLowerCase().contains("already exists") || ex.getMessage().toLowerCase().contains("duplicate"))) {
            response.put("error", "Conflict");
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            response.put("error", "Not Found");
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("invalid email or password")) {
            response.put("error", "Unauthorized");
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            response.put("error", "Not Found");
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        if (ex.getMessage() != null && (ex.getMessage().toLowerCase().contains("already exists") || ex.getMessage().toLowerCase().contains("duplicate"))) {
            response.put("error", "Conflict");
            response.put("message", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        response.put("error", "Application Error");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
