package com.golden.erp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import feign.FeignException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                "Dados inválidos",
                errors,
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()
        );

        logger.warn("Erro de validação: {}", errors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();

        // Determina o status code baseado na mensagem
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (message.contains("não encontrado")) {
            status = HttpStatus.NOT_FOUND;
        } else if (message.contains("já está em uso") || message.contains("já existe")) {
            status = HttpStatus.CONFLICT;
        } else if (message.contains("CEP inválido") || message.contains("inválido")) {
            status = HttpStatus.BAD_REQUEST;
        } else if (message.contains("estoque insuficiente") || message.contains("regra")) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                null,
                LocalDateTime.now(),
                status.value()
        );

        logger.error("Erro de negócio: {}", message, ex);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        String message = "Erro na integração externa";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (ex.status() == 400) {
            message = "CEP inválido";
        } else if (ex.status() >= 500) {
            message = "Serviço externo temporariamente indisponível";
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                null,
                LocalDateTime.now(),
                status.value()
        );

        logger.error("Erro na integração Feign: {}", ex.getMessage(), ex);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Erro interno do servidor",
                null,
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        logger.error("Erro não tratado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    public static class ErrorResponse {
        private String message;
        private Map<String, String> errors;
        private LocalDateTime timestamp;
        private int status;

        public ErrorResponse(String message, Map<String, String> errors, LocalDateTime timestamp, int status) {
            this.message = message;
            this.errors = errors;
            this.timestamp = timestamp;
            this.status = status;
        }

        // Getters and Setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public void setErrors(Map<String, String> errors) {
            this.errors = errors;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
