package com.advancedBigDataIndexing.PlanService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JsonSchemaValidationException.class)
    public ResponseEntity<Map<String, Object>> handleJsonSchemaValidationException(JsonSchemaValidationException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
