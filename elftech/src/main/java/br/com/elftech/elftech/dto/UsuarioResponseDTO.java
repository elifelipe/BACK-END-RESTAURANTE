package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.Usuario;
import lombok.Data;

import java.util.UUID;

@Data
public class UsuarioResponseDTO {
    private UUID id;
    private String login;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.login = usuario.getLogin();
    }
}