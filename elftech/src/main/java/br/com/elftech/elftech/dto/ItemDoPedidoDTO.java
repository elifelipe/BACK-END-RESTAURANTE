package br.com.elftech.elftech.dto;

import java.util.UUID;
// DTO para um item individual no carrinho
public record ItemDoPedidoDTO(UUID itemCardapioId, int quantidade) {}
