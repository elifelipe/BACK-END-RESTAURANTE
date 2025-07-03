package br.com.elftech.elftech.dto;

import java.util.List;
import java.util.UUID;

// Adicionado 'Integer numeroMesa'
public record CriarPedidoRequest(UUID restauranteId, Integer numeroMesa, List<ItemDoPedidoDTO> itens) {
}