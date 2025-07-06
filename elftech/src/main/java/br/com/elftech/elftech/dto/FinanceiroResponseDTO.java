package br.com.elftech.elftech.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que representa a resposta consolidada da consulta financeira.
 */
public record FinanceiroResponseDTO(
        BigDecimal receitaTotal,
        BigDecimal lucroTotal,
        Integer totalPedidos,
        List<PedidoFinanceiroDTO> pedidos
) {}
