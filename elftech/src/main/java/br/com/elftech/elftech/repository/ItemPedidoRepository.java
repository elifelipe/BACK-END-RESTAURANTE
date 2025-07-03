package br.com.elftech.elftech.repository;

import br.com.elftech.elftech.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// ItemPedidoRepository.java
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, UUID> { }
