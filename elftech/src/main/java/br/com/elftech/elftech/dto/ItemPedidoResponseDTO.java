package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.ItemPedido;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ItemPedidoResponseDTO {
    private String nomeItem;
    private int quantidade;
    private BigDecimal precoUnitario;

    public ItemPedidoResponseDTO(ItemPedido itemPedido) {
        this.nomeItem = itemPedido.getItemCardapio().getNome();
        this.quantidade = itemPedido.getQuantidade();
        this.precoUnitario = itemPedido.getPrecoUnitario();
    }
}