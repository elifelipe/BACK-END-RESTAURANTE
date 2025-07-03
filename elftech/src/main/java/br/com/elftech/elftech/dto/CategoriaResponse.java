package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.Categoria;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoriaResponse {
    private UUID id;
    private String nome;

    public CategoriaResponse(Categoria categoria) {
        this.id = categoria.getId();
        this.nome = categoria.getNome();
    }
}