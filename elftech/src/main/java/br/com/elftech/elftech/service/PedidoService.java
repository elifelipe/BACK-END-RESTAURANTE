package br.com.elftech.elftech.service;

import br.com.elftech.elftech.dto.CriarPedidoRequest;
import br.com.elftech.elftech.dto.ItemDoPedidoDTO;
import br.com.elftech.elftech.dto.PedidoResponseDTO;
import br.com.elftech.elftech.dto.StatusPedidoResponseDTO;
import br.com.elftech.elftech.model.*;
import br.com.elftech.elftech.repository.ItemCardapioRepository;
import br.com.elftech.elftech.repository.PedidoRepository;
import br.com.elftech.elftech.repository.RestauranteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ItemCardapioRepository itemCardapioRepository;

    /**
     * Cria um novo pedido. Este método é chamado por um endpoint público.
     * @param dados Os dados da requisição contendo o restaurante e os itens.
     * @return A entidade Pedido recém-criada.
     */
    @Transactional
    public Pedido criarPedido(CriarPedidoRequest dados) {
        Restaurante restaurante = restauranteRepository.findById(dados.restauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        // --- LÓGICA DE NUMERAÇÃO DO PEDIDO ---
        // 1. Busca o último pedido feito para este restaurante.
        long proximoNumero = pedidoRepository.findTopByRestauranteIdOrderByNumeroPedidoDesc(dados.restauranteId())
                .map(ultimoPedido -> ultimoPedido.getNumeroPedido() + 1) // 2. Se existir, pega o número dele e soma 1.
                .orElse(1L); // 3. Se não existir (primeiro pedido), começa com 1.

        Pedido pedido = new Pedido();
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(Pedido.StatusPedido.PENDENTE);
        pedido.setNumeroPedido(proximoNumero); // Define o número sequencial
        pedido.setNumeroMesa(dados.numeroMesa()); // Define o número da mesa vindo do DTO

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemDoPedidoDTO itemDTO : dados.itens()) {
            ItemCardapio itemCardapio = itemCardapioRepository.findById(itemDTO.itemCardapioId())
                    .orElseThrow(() -> new EntityNotFoundException("Item de cardápio com ID " + itemDTO.itemCardapioId() + " não encontrado."));

            if (!itemCardapio.isDisponivel()) {
                throw new IllegalStateException("O item '" + itemCardapio.getNome() + "' não está disponível no momento.");
            }

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(pedido);
            itemPedido.setItemCardapio(itemCardapio);
            itemPedido.setQuantidade(itemDTO.quantidade());
            itemPedido.setPrecoUnitario(itemCardapio.getPreco()); // Preço do banco de dados, para segurança

            pedido.getItens().add(itemPedido);
            valorTotal = valorTotal.add(itemCardapio.getPreco().multiply(BigDecimal.valueOf(itemDTO.quantidade())));
        }

        pedido.setValorTotal(valorTotal);
        return pedidoRepository.save(pedido);
    }

    /**
     * Lista os pedidos de um restaurante específico para o painel de admin.
     * Requer autenticação.
     * @return Uma lista de PedidoResponseDTO.
     */
    public List<PedidoResponseDTO> listarPedidosPorRestaurante(UUID restauranteId) {
        validarAcessoRestaurante(restauranteId); // Garante que o admin só veja seus próprios pedidos
        List<Pedido> pedidos = pedidoRepository.findByRestauranteIdOrderByDataPedidoDesc(restauranteId);
        return pedidos.stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Marca um pedido como CONCLUIDO.
     * Requer autenticação.
     * @return O PedidoResponseDTO atualizado.
     */
    @Transactional
    public PedidoResponseDTO marcarComoConcluido(UUID restauranteId, UUID pedidoId) {
        validarAcessoRestaurante(restauranteId); // Garante a permissão
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido com ID " + pedidoId + " não encontrado."));

        if (!pedido.getRestaurante().getId().equals(restauranteId)) {
            throw new SecurityException("Este pedido não pertence ao restaurante informado.");
        }

        pedido.setStatus(Pedido.StatusPedido.CONCLUIDO);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        return new PedidoResponseDTO(pedidoSalvo);
    }

    public StatusPedidoResponseDTO getStatusPedido(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido com ID " + pedidoId + " não encontrado."));
        return new StatusPedidoResponseDTO(pedido.getStatus());
    }

    /**
     * Valida se o usuário logado pertence ao restaurante que está tentando acessar.
     */
    private void validarAcessoRestaurante(UUID restauranteIdUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Acesso negado: usuário não autenticado.");
        }
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        if (usuarioLogado.getRestaurante() == null) {
            throw new SecurityException("Acesso negado: usuário sem restaurante associado.");
        }
        if (!usuarioLogado.getRestaurante().getId().equals(restauranteIdUrl)) {
            throw new SecurityException("Acesso negado: o usuário não tem permissão para acessar este restaurante.");
        }
    }
}