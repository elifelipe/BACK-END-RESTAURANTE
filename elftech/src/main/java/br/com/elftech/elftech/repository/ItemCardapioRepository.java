package br.com.elftech.elftech.repository;

import br.com.elftech.elftech.model.ItemCardapio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItemCardapioRepository extends JpaRepository<ItemCardapio, UUID> {

    List<ItemCardapio> findByRestauranteId(UUID restauranteId);
    boolean existsByCategoriaId(UUID categoriaId);

    // Adicione este método:
    List<ItemCardapio> findByRestauranteIdAndDisponivelTrue(UUID restauranteId);
}