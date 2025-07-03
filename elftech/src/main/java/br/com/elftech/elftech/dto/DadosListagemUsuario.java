package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.Usuario;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) para retornar uma lista de usuários de forma segura.
 * Ele expõe apenas os dados necessários, omitindo informações sensíveis como a senha.
 */
public record DadosListagemUsuario(UUID id, String login, String role, UUID restauranteId) {

    /**
     * Construtor que facilita a conversão de uma entidade 'Usuario' para este DTO.
     * @param usuario A entidade JPA a ser convertida.
     */
    public DadosListagemUsuario(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getRole(),
                // Verifica se o restaurante não é nulo antes de pegar o ID para evitar NullPointerException
                usuario.getRestaurante() != null ? usuario.getRestaurante().getId() : null
        );
    }
}
