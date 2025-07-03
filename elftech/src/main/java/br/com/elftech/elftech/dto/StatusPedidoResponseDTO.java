package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.Pedido;

public record StatusPedidoResponseDTO(Pedido.StatusPedido status) {
}