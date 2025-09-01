package com.golden.erp.exception;

/**
 * Exceção específica para CEP inválido.
 * Lançada quando um CEP não é encontrado ou é inválido no serviço ViaCEP.
 */
public class CepInvalidoException extends RuntimeException {

    public CepInvalidoException(String message) {
        super(message);
    }

    public CepInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
