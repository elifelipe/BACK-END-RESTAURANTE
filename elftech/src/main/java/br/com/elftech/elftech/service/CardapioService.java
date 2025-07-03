package br.com.elftech.elftech.service;

import br.com.elftech.elftech.dto.CriarItemRequest;
import br.com.elftech.elftech.dto.ItemCardapioResponse;
import br.com.elftech.elftech.model.Categoria;
import br.com.elftech.elftech.model.ItemCardapio;
import br.com.elftech.elftech.model.Restaurante;
import br.com.elftech.elftech.model.Usuario;
import br.com.elftech.elftech.repository.CategoriaRepository;
import br.com.elftech.elftech.repository.ItemCardapioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CardapioService {

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // ============================================================================
    // === MÉTODO CORRIGIDO PARA SER PÚBLICO ===
    // ============================================================================
    public List<ItemCardapioResponse> listarItensPorRestaurante(UUID restauranteId) {
        // REMOVEMOS a chamada para validarAcessoRestaurante(restauranteId) daqui.

        // Melhoria: Usamos um método que busca apenas itens disponíveis para o cardápio público.
        // Garanta que o método findByRestauranteIdAndDisponivelTrue exista no seu ItemCardapioRepository.
        List<ItemCardapio> itens = itemCardapioRepository.findByRestauranteIdAndDisponivelTrue(restauranteId);

        return itens.stream()
                .map(ItemCardapioResponse::new)
                .collect(Collectors.toList());
    }
    // ============================================================================

    @Transactional
    public ItemCardapioResponse criarItem(CriarItemRequest request, Restaurante restaurante) {
        validarAcessoRestaurante(restaurante.getId());

        Categoria categoria = categoriaRepository
                .findByNomeIgnoreCaseAndRestauranteId(request.categoriaNome(), restaurante.getId())
                .orElseGet(() -> {
                    Categoria novaCategoria = new Categoria();
                    novaCategoria.setNome(request.categoriaNome());
                    novaCategoria.setRestaurante(restaurante);
                    return categoriaRepository.save(novaCategoria);
                });

        ItemCardapio novoItem = new ItemCardapio();
        novoItem.setNome(request.nome());
        novoItem.setDescricao(request.descricao());
        novoItem.setPreco(request.preco());
        novoItem.setRestaurante(restaurante);
        novoItem.setCategoria(categoria);
        novoItem.setFotoUrl(request.fotoUrl());
        novoItem.setDisponivel(request.disponivel() != null ? request.disponivel() : true);

        ItemCardapio itemSalvo = itemCardapioRepository.save(novoItem);
        return new ItemCardapioResponse(itemSalvo);
    }

    @Transactional
    public ItemCardapioResponse atualizarItem(UUID restauranteId, UUID itemId, CriarItemRequest request) {
        validarAcessoRestaurante(restauranteId);
        ItemCardapio item = itemCardapioRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item do cardápio não encontrado"));

        Categoria categoria = categoriaRepository
                .findByNomeIgnoreCaseAndRestauranteId(request.categoriaNome(), restauranteId)
                .orElseGet(() -> {
                    Restaurante r = new Restaurante();
                    r.setId(restauranteId);
                    Categoria novaCategoria = new Categoria();
                    novaCategoria.setNome(request.categoriaNome());
                    novaCategoria.setRestaurante(r);
                    return categoriaRepository.save(novaCategoria);
                });

        item.setNome(request.nome());
        item.setDescricao(request.descricao());
        item.setPreco(request.preco());
        item.setCategoria(categoria);
        item.setFotoUrl(request.fotoUrl());
        item.setDisponivel(request.disponivel());

        return new ItemCardapioResponse(itemCardapioRepository.save(item));
    }

    @Transactional
    public void deletarItem(UUID restauranteId, UUID itemId) {
        validarAcessoRestaurante(restauranteId);
        if (!itemCardapioRepository.existsById(itemId)) {
            throw new RuntimeException("Item do cardápio não encontrado");
        }
        itemCardapioRepository.deleteById(itemId);
    }

    @Transactional
    public void alternarDisponibilidade(UUID restauranteId, UUID itemId) {
        validarAcessoRestaurante(restauranteId);
        ItemCardapio item = itemCardapioRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado."));
        item.setDisponivel(!item.isDisponivel());
        itemCardapioRepository.save(item);
    }

    // --- MÉTODO DE VALIDAÇÃO (INTOCADO, CONTINUA PERFEITO PARA AS ROTAS DE ADMIN) ---
    private void validarAcessoRestaurante(UUID restauranteIdUrl) {
        System.out.println("\n--- INICIANDO VALIDAÇÃO DE ACESSO (CardapioService) ---");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            System.err.println("FALHA NA VALIDAÇÃO: Usuário não está autenticado.");
            throw new SecurityException("Acesso negado: usuário não autenticado.");
        }

        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        System.out.println("Usuário Logado Encontrado: " + usuarioLogado.getLogin());

        if (usuarioLogado.getRestaurante() == null) {
            System.err.println("FALHA CRÍTICA: O usuário '" + usuarioLogado.getLogin() + "' não está associado a nenhum restaurante no banco de dados.");
            throw new SecurityException("Acesso negado: usuário sem restaurante associado.");
        }

        UUID restauranteIdDoUsuario = usuarioLogado.getRestaurante().getId();
        System.out.println("ID do Restaurante na URL da requisição: " + restauranteIdUrl);
        System.out.println("ID do Restaurante associado ao Token do Usuário: " + restauranteIdDoUsuario);

        if (!restauranteIdDoUsuario.equals(restauranteIdUrl)) {
            System.err.println("!!! ACESSO NEGADO: IDs de restaurante NÃO BATEM !!!");
            throw new SecurityException("Acesso negado. O usuário não tem permissão para acessar este restaurante.");
        }

        System.out.println("--- ACESSO VALIDADO COM SUCESSO (CardapioService) ---");
    }
}