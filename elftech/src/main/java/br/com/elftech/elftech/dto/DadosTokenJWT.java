package br.com.elftech.elftech.dto;

/**
 * Representa os dados do token JWT que serão enviados na resposta do login.
 */
public record DadosTokenJWT(String token) {
}