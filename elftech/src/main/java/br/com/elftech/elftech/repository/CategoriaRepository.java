package br.com.elftech.elftech.repository;

import br.com.elftech.elftech.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    List<Categoria> findByRestauranteId(UUID restauranteId);

    Optional<Categoria> findByNomeIgnoreCaseAndRestauranteId(String nome, UUID restauranteId);
}