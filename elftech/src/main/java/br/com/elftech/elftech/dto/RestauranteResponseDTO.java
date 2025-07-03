package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.Restaurante;
import lombok.Data;

import java.util.UUID;

@Data
public class RestauranteResponseDTO {
    private UUID id;
    private String nome;
    private String cnpj;

    // Este construtor facilita a conversão da nossa Entidade para este DTO
    public RestauranteResponseDTO(Restaurante restaurante) {
        this.id = restaurante.getId();
        this.nome = restaurante.getNome();
        this.cnpj = restaurante.getCnpj();
    }
}