package br.com.elftech.elftech.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // --- NOVO CAMPO ---
    // Armazena o número sequencial do pedido para um restaurante específico (Ex: Pedido #1, #2, etc.)
    @Column(nullable = false)
    private Long numeroPedido;

    // --- NOVO CAMPO ---
    // Armazena o número da mesa onde o pedido foi feito
    @Column(nullable = false)
    private Integer numeroMesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @Column(nullable = false)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private LocalDateTime dataPedido;

    public enum StatusPedido {
        PENDENTE,
        CONCLUIDO,
        CANCELADO
    }
}
