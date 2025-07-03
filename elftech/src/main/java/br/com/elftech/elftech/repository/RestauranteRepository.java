package br.com.elftech.elftech.repository;

import br.com.elftech.elftech.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- Lembre-se deste import
import org.springframework.stereotype.Repository;

import java.util.List; // <-- Lembre-se deste import
import java.util.UUID;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, UUID> {

    boolean existsByCnpj(String cnpj);

    // --- MÉTODO CUSTOMIZADO QUE FALTAVA ---
    // O 'LEFT JOIN FETCH' diz ao JPA para buscar os usuários na mesma consulta,
    // resolvendo o problema de performance N+1 e garantindo que os dados estejam disponíveis.
    @Query("SELECT r FROM Restaurante r LEFT JOIN FETCH r.usuarios WHERE r.ativo = true")
    List<Restaurante> findAllWithUsers();
}