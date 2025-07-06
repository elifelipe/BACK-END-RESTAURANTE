package br.com.elftech.elftech.repository;

import br.com.elftech.elftech.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByRestauranteIdOrderByDataPedidoDesc(UUID restauranteId);

    Optional<Pedido> findTopByRestauranteIdOrderByNumeroPedidoDesc(UUID restauranteId);

    // --- NOVO MÉTODO PARA BUSCA FINANCEIRA ---
    /**
     * Encontra todos os pedidos de um restaurante, dentro de um intervalo de datas e com status específicos.
     * Usado para calcular os dados financeiros.
     * @param restauranteId O ID do restaurante.
     * @param inicio A data e hora de início do período.
     * @param fim A data e hora de fim do período.
     * @param status A lista de status de pedido a serem incluídos (ex: CONCLUIDO, ENTREGUE).
     * @return Uma lista de pedidos que correspondem aos critérios.
     */
    List<Pedido> findByRestauranteIdAndDataPedidoBetweenAndStatusIn(
            UUID restauranteId,
            LocalDateTime inicio,
            LocalDateTime fim,
            List<Pedido.StatusPedido> status
    );
}
