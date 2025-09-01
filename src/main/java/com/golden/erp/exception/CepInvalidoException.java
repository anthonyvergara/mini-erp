package com.golden.erp.exception;

public class CepInvalidoException extends RuntimeException {

    public CepInvalidoException(String message) {
        super(message);
    }

    public CepInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
