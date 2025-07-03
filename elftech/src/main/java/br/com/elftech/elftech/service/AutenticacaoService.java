package br.com.elftech.elftech.service;

import br.com.elftech.elftech.dto.DadosListagemUsuario;
import br.com.elftech.elftech.dto.DadosRegistro;
import br.com.elftech.elftech.exception.ResourceNotFoundException;
import br.com.elftech.elftech.model.Restaurante; // Certifique-se de importar o Restaurante
import br.com.elftech.elftech.model.Usuario;
import br.com.elftech.elftech.repository.RestauranteRepository; // Importe o repositório do Restaurante
import br.com.elftech.elftech.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException; // Uma exceção mais apropriada
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. INJETE O REPOSITÓRIO DE RESTAURANTE
    @Autowired
    private RestauranteRepository restauranteRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o login: " + username));
    }

    // NOVO MÉTODO: Listar todos os usuários
    public List<DadosListagemUsuario> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(DadosListagemUsuario::new) // Converte cada Usuario para o DTO
                .collect(Collectors.toList());
    }

    // NOVO MÉTODO: Deletar um usuário por ID
    @Transactional
    public void deletarUsuario(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            // Lança uma exceção se o
            //
            // usuário não for encontrado
            throw new ResourceNotFoundException("Usuário não encontrado com o ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // 2. CORRIJA A ASSINATURA E A LÓGICA DO MÉTODO
    // Removemos o 'Usuario adminLogado' e a lógica agora busca o restaurante pelo ID
    public void criarUsuario(DadosRegistro dados) {
        if (usuarioRepository.findByLogin(dados.login()).isPresent()) {
            throw new RuntimeException("Login já está em uso.");
        }

        // Busca o restaurante pelo ID que veio do front-end
        Restaurante restaurante = restauranteRepository.findById(dados.restauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante com o ID " + dados.restauranteId() + " não foi encontrado."));

        Usuario novoUsuario = new Usuario();
        novoUsuario.setLogin(dados.login());
        novoUsuario.setSenha(passwordEncoder.encode(dados.senha()));
        // Associa o novo usuário ao restaurante encontrado
        novoUsuario.setRestaurante(restaurante);

        usuarioRepository.save(novoUsuario);
    }
}