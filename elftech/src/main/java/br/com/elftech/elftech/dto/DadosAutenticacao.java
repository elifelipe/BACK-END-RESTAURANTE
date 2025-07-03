package br.com.elftech.elftech.dto;

/**
 * Representa os dados que chegam na requisição de login.
 * O 'record' já cria os campos, construtor e getters automaticamente.
 */
public record DadosAutenticacao(String login, String senha) {
}