package br.com.elftech.elftech.service;

import br.com.elftech.elftech.dto.CategoriaRequest;
import br.com.elftech.elftech.dto.CategoriaResponse;
import br.com.elftech.elftech.model.Categoria;
import br.com.elftech.elftech.model.Restaurante;
import br.com.elftech.elftech.model.Usuario;
import br.com.elftech.elftech.repository.CategoriaRepository;
import br.com.elftech.elftech.repository.ItemCardapioRepository;
import br.com.elftech.elftech.repository.RestauranteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private RestauranteRepository restauranteRepository;
    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Transactional
    public Categoria criarCategoria(UUID restauranteId, CategoriaRequest request) {
        validarAcessoRestaurante(restauranteId); // Adicionando validação aqui
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado"));
        categoriaRepository.findByNomeIgnoreCaseAndRestauranteId(request.getNome(), restauranteId)
                .ifPresent(c -> { throw new RuntimeException("Categoria com este nome já existe."); });
        Categoria novaCategoria = new Categoria();
        novaCategoria.setNome(request.getNome());
        novaCategoria.setRestaurante(restaurante);
        return categoriaRepository.save(novaCategoria);
    }

    public List<CategoriaResponse> listarCategoriasPorRestaurante(UUID restauranteId) {
        validarAcessoRestaurante(restauranteId); // Adicionando validação aqui
        return categoriaRepository.findByRestauranteId(restauranteId).stream()
                .map(CategoriaResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaResponse atualizarCategoria(UUID restauranteId, UUID categoriaId, CategoriaRequest request) {
        validarAcessoRestaurante(restauranteId); // Adicionando validação aqui
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        categoria.setNome(request.getNome());
        return new CategoriaResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void deletarCategoria(UUID restauranteId, UUID categoriaId) {
        validarAcessoRestaurante(restauranteId); // Adicionando validação aqui
        if (itemCardapioRepository.existsByCategoriaId(categoriaId)) {
            throw new RuntimeException("Não é possível deletar a categoria, pois ela está sendo utilizada por itens do cardápio.");
        }
        categoriaRepository.deleteById(categoriaId);
    }

    private void validarAcessoRestaurante(UUID restauranteIdUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Acesso negado: usuário não autenticado.");
        }
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        if (usuarioLogado.getRestaurante() == null) {
            throw new SecurityException("Acesso negado: usuário sem restaurante associado.");
        }
        UUID restauranteIdDoUsuario = usuarioLogado.getRestaurante().getId();
        if (!restauranteIdDoUsuario.equals(restauranteIdUrl)) {
            throw new SecurityException("Acesso negado. O usuário não tem permissão para acessar este restaurante.");
        }
    }
}