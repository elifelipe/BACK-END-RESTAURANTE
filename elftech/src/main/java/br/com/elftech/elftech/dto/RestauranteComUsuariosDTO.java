package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.Restaurante;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class RestauranteComUsuariosDTO {
    private UUID id;
    private String nome;
    private String cnpj;
    private List<UsuarioResponseDTO> usuarios;

    public RestauranteComUsuariosDTO(Restaurante restaurante) {
        this.id = restaurante.getId();
        this.nome = restaurante.getNome();
        this.cnpj = restaurante.getCnpj();
        // Mapeia a lista de entidades Usuario para uma lista de DTOs UsuarioResponseDTO
        this.usuarios = restaurante.getUsuarios().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }
}