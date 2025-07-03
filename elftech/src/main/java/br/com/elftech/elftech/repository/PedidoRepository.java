package br.com.elftech.elftech.repository;

import br.com.elftech.elftech.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByRestauranteIdOrderByDataPedidoDesc(UUID restauranteId);

    // --- NOVO MÉTODO ---
    // Busca o último pedido (pelo maior numeroPedido) de um restaurante específico.
    Optional<Pedido> findTopByRestauranteIdOrderByNumeroPedidoDesc(UUID restauranteId);
}