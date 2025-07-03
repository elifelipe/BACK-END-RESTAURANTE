package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.Pedido;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class PedidoResponseDTO {
    private UUID id;
    private Long numeroPedido; // NOVO
    private Integer numeroMesa; // NOVO
    private Pedido.StatusPedido status;
    private BigDecimal valorTotal;
    private LocalDateTime dataPedido;
    private List<ItemPedidoResponseDTO> itens;

    public PedidoResponseDTO(Pedido pedido) {
        this.id = pedido.getId();
        this.numeroPedido = pedido.getNumeroPedido(); // NOVO
        this.numeroMesa = pedido.getNumeroMesa(); // NOVO
        this.status = pedido.getStatus();
        this.valorTotal = pedido.getValorTotal();
        this.dataPedido = pedido.getDataPedido();
        // Aqui convertemos a lista de Entidades ItemPedido para uma lista de DTOs
        this.itens = pedido.getItens().stream()
                .map(ItemPedidoResponseDTO::new)
                .collect(Collectors.toList());
    }
}