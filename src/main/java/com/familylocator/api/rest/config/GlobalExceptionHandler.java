package com.familylocator.api.rest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException ex) {
    log.warn("Validation error: {}", ex.getMessage());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Validation failed");

    Map<String, String> fieldErrors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
    body.put("errors", fieldErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    log.warn("Business validation error: {}", ex.getMessage());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
    body.put("error", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
  }
}
