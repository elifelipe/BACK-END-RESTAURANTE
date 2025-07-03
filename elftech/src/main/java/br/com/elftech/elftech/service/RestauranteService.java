package br.com.elftech.elftech.service;

import br.com.elftech.elftech.dto.CriarRestauranteRequest;
import br.com.elftech.elftech.dto.RestauranteComUsuariosDTO;
import br.com.elftech.elftech.dto.RestauranteResponseDTO; // Importe o DTO
import br.com.elftech.elftech.model.Restaurante;
import br.com.elftech.elftech.model.Usuario;
import br.com.elftech.elftech.repository.RestauranteRepository;
import br.com.elftech.elftech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RestauranteService {

    @Autowired
    private RestauranteRepository restauranteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<RestauranteComUsuariosDTO> listarRestaurantes() {
        return restauranteRepository.findAllWithUsers().stream()
                .map(RestauranteComUsuariosDTO::new)
                .collect(Collectors.toList());
    }

    // --- NOVO MÉTODO PÚBLICO ---
    // Este método NÃO tem validação de segurança
    public RestauranteResponseDTO buscarPublicoPorId(UUID restauranteId) {
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
        return new RestauranteResponseDTO(restaurante);
    }

    // --- MÉTODO ATUALIZADO ---
    // Agora retorna o DTO diretamente, garantindo que tudo seja resolvido dentro da transação.
    public RestauranteResponseDTO buscarPorId(UUID restauranteId) {
        validarAcessoRestaurante(restauranteId);
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
        // A conversão para DTO acontece aqui dentro do serviço
        return new RestauranteResponseDTO(restaurante);
    }

    // ... O resto dos métodos (criarRestaurante, deletarRestaurante, etc.) continua igual
    @Transactional
    public Restaurante criarRestaurante(CriarRestauranteRequest request) {
        if (restauranteRepository.existsByCnpj(request.getCnpj())) {
            throw new RuntimeException("Já existe um restaurante com este CNPJ.");
        }
        if (usuarioRepository.findByLogin(request.getAdminLogin()).isPresent()){
            throw new RuntimeException("Este login de administrador já está em uso.");
        }

        Restaurante novoRestaurante = new Restaurante();
        novoRestaurante.setNome(request.getNome());
        novoRestaurante.setCnpj(request.getCnpj());
        restauranteRepository.save(novoRestaurante);

        Usuario adminUser = new Usuario();
        adminUser.setLogin(request.getAdminLogin());
        adminUser.setSenha(passwordEncoder.encode(request.getAdminSenha()));
        adminUser.setRestaurante(novoRestaurante);

        // --- LINHA ADICIONADA AQUI ---
        // Definimos que este usuario sempre será um administrador
        adminUser.setRole("ROLE_ADMIN");

        usuarioRepository.save(adminUser);

        return novoRestaurante;
    }

    @Transactional
    public void deletarRestaurante(UUID restauranteId) {
        validarAcessoRestaurante(restauranteId);
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
        restaurante.setAtivo(false);
        restauranteRepository.save(restaurante);
    }

    private void validarAcessoRestaurante(UUID restauranteIdUrl) {
        Usuario usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID restauranteIdDoUsuario = usuarioLogado.getRestaurante().getId();
        if (!restauranteIdDoUsuario.equals(restauranteIdUrl)) {
            throw new SecurityException("Acesso negado.");
        }
    }
}