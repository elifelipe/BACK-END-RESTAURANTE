package br.com.elftech.elftech.repository;

import br.com.elftech.elftech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    // Método que o Spring Security usará para buscar um usuário pelo login
    Optional<UserDetails> findByLogin(String login);
}