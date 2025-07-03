package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.ItemCardapio;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ItemCardapioResponse {

    // CAMPOS QUE ESTAVAM FALTANDO
    private UUID id;
    private String nome;
    private String descricao;
    private BigDecimal preco;

    // CAMPOS QUE VOCÊ JÁ TINHA
    private String categoriaNome;
    private String fotoUrl;
    private boolean disponivel;

    // O construtor precisa preencher TODOS os campos
    public ItemCardapioResponse(ItemCardapio item) {
        this.id = item.getId();
        this.nome = item.getNome();
        this.descricao = item.getDescricao();
        this.preco = item.getPreco();
        this.categoriaNome = item.getCategoria().getNome();
        this.fotoUrl = item.getFotoUrl();
        this.disponivel = item.isDisponivel();
    }
}