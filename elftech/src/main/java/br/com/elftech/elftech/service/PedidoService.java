package br.com.elftech.elftech.service;

import br.com.elftech.elftech.dto.CriarPedidoRequest;
import br.com.elftech.elftech.dto.ItemDoPedidoDTO;
import br.com.elftech.elftech.dto.PedidoResponseDTO;
import br.com.elftech.elftech.dto.PedidosPorMesResponseDTO; // Importe o novo DTO
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
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Transactional
    public Pedido criarPedido(CriarPedidoRequest dados) {
        Restaurante restaurante = restauranteRepository.findById(dados.restauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        long proximoNumero = pedidoRepository.findTopByRestauranteIdOrderByNumeroPedidoDesc(dados.restauranteId())
                .map(ultimoPedido -> ultimoPedido.getNumeroPedido() + 1)
                .orElse(1L);

        Pedido pedido = new Pedido();
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(Pedido.StatusPedido.PENDENTE);
        pedido.setNumeroPedido(proximoNumero);
        pedido.setNumeroMesa(dados.numeroMesa());

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
            itemPedido.setPrecoUnitario(itemCardapio.getPreco());

            pedido.getItens().add(itemPedido);
            valorTotal = valorTotal.add(itemCardapio.getPreco().multiply(BigDecimal.valueOf(itemDTO.quantidade())));
        }

        pedido.setValorTotal(valorTotal);

        // --- LÓGICA DE CÁLCULO DE LUCRO ADICIONADA ---
        // Exemplo: Lucro bruto de 30% sobre o valor total.
        // Você pode ajustar esta regra de negócio conforme necessário.
        BigDecimal lucroCalculado = valorTotal.multiply(new BigDecimal("0.30"));
        pedido.setLucro(lucroCalculado);

        return pedidoRepository.save(pedido);
    }

    public List<PedidoResponseDTO> listarPedidosPorRestaurante(UUID restauranteId) {
        validarAcessoRestaurante(restauranteId);
        List<Pedido> pedidos = pedidoRepository.findByRestauranteIdOrderByDataPedidoDesc(restauranteId);
        return pedidos.stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PedidoResponseDTO marcarComoConcluido(UUID restauranteId, UUID pedidoId) {
        validarAcessoRestaurante(restauranteId);
        Pedido pedido = getPedidoValidado(restauranteId, pedidoId);

        if (pedido.getStatus() != Pedido.StatusPedido.PENDENTE) {
            throw new IllegalStateException("Apenas pedidos pendentes podem ser marcados como concluídos.");
        }

        pedido.setStatus(Pedido.StatusPedido.CONCLUIDO);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        return new PedidoResponseDTO(pedidoSalvo);
    }

    @Transactional
    public PedidoResponseDTO marcarComoEntregue(UUID restauranteId, UUID pedidoId) {
        validarAcessoRestaurante(restauranteId); // Garante a permissão
        Pedido pedido = getPedidoValidado(restauranteId, pedidoId);

        if (pedido.getStatus() != Pedido.StatusPedido.CONCLUIDO) {
            throw new IllegalStateException("O pedido não pode ser marcado como entregue, pois não está pronto.");
        }

        pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        return new PedidoResponseDTO(pedidoSalvo);
    }

    public StatusPedidoResponseDTO getStatusPedido(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido com ID " + pedidoId + " não encontrado."));
        return new StatusPedidoResponseDTO(pedido.getStatus());
    }

    /**
     * NOVO MÉTODO: Retorna o total de pedidos por mês para um determinado ano.
     * Considera apenas pedidos com status CONCLUIDO ou ENTREGUE.
     * @param restauranteId O ID do restaurante.
     * @param ano O ano para o qual os dados serão buscados.
     * @return Uma lista de PedidosPorMesResponseDTO com o total de pedidos por mês.
     */
    public List<PedidosPorMesResponseDTO> getPedidosPorMesDoAno(UUID restauranteId, int ano) {
        validarAcessoRestaurante(restauranteId);

        List<PedidosPorMesResponseDTO> dadosAnuais = new ArrayList<>();
        List<Pedido.StatusPedido> statusValidos = Arrays.asList(Pedido.StatusPedido.CONCLUIDO, Pedido.StatusPedido.ENTREGUE);

        for (Month month : Month.values()) {
            YearMonth yearMonth = YearMonth.of(ano, month);
            LocalDateTime inicioDoMes = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime fimDoMes = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<Pedido> pedidosNoMes = pedidoRepository.findByRestauranteIdAndDataPedidoBetweenAndStatusIn(
                    restauranteId,
                    inicioDoMes,
                    fimDoMes,
                    statusValidos
            );

            long totalPedidos = pedidosNoMes.size();
            dadosAnuais.add(new PedidosPorMesResponseDTO(month.getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "BR")), totalPedidos));
        }
        return dadosAnuais;
    }

    private Pedido getPedidoValidado(UUID restauranteId, UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido com ID " + pedidoId + " não encontrado."));

        if (!pedido.getRestaurante().getId().equals(restauranteId)) {
            throw new SecurityException("Este pedido não pertence ao restaurante informado.");
        }
        return pedido;
    }

    private void validarAcessoRestaurante(UUID restauranteIdUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Acesso negado: usuário não autenticado.");
        }
        // Assumindo que o principal é um objeto Usuario ou que possui um método para obter o ID do restaurante
        // Esta parte pode precisar de ajuste dependendo da sua implementação de segurança (UserDetails, etc.)
        if (!(authentication.getPrincipal() instanceof Usuario)) {
            throw new SecurityException("Tipo de principal inesperado. Não é um objeto Usuario.");
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
