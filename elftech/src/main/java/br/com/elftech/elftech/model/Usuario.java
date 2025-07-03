package br.com.elftech.elftech.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String login;
    private String senha;

    // --- NOVO CAMPO ADICIONADO AQUI ---
    private String role; // Armazenará "ROLE_ADMIN" ou "ROLE_USER"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    // --- MÉTODO ATUALIZADO ---
    // Agora, as permissões são baseadas no campo 'role'
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if ("ROLE_ADMIN".equals(this.role)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        // Por padrão, ou se a role for nula/diferente, será um usuário comum.
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
