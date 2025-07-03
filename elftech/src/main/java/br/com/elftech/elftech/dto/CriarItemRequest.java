package br.com.elftech.elftech.dto;

import java.math.BigDecimal;

public record CriarItemRequest(
        String nome,
        String descricao,
        BigDecimal preco,
        String categoriaNome,
        String fotoUrl,
        Boolean disponivel
) {}