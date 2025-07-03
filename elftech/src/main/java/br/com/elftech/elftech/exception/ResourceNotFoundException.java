package br.com.elftech.elftech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção customizada para ser lançada quando um recurso específico não é encontrado no sistema.
 * A anotação @ResponseStatus(HttpStatus.NOT_FOUND) garante que, quando esta exceção
 * não for tratada, o Spring automaticamente retornará uma resposta HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
