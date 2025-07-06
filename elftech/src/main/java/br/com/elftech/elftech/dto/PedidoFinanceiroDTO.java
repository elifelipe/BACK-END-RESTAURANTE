package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO simplificado para representar um pedido na lista da resposta financeira.
 */
public record PedidoFinanceiroDTO(
        Long numeroPedido,
        BigDecimal valorTotal,
        BigDecimal lucro,
        LocalDateTime dataPedido
) {
    // Construtor para facilitar a conversão da entidade Pedido para este DTO
    public PedidoFinanceiroDTO(Pedido pedido) {
        this(
                pedido.getNumeroPedido(),
                pedido.getValorTotal(),
                pedido.getLucro(),
                pedido.getDataPedido()
        );
    }
}
