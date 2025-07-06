package br.com.elftech.elftech.service;

import br.com.elftech.elftech.dto.FinanceiroMensalResponseDTO;
import br.com.elftech.elftech.dto.FinanceiroResponseDTO;
import br.com.elftech.elftech.dto.PedidoFinanceiroDTO;
import br.com.elftech.elftech.dto.PedidosPorDiaDTO;
import br.com.elftech.elftech.model.Pedido;
import br.com.elftech.elftech.model.Usuario;
import br.com.elftech.elftech.repository.PedidoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinanceiroService {

    private final PedidoRepository pedidoRepository;

    public FinanceiroService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public FinanceiroResponseDTO buscarDados(UUID restauranteId, LocalDate data) {
        validarAcessoRestaurante(restauranteId);
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(23, 59, 59);
        List<Pedido.StatusPedido> statusValidos = List.of(Pedido.StatusPedido.CONCLUIDO, Pedido.StatusPedido.ENTREGUE);

        List<Pedido> pedidosDoDia = pedidoRepository.findByRestauranteIdAndDataPedidoBetweenAndStatusIn(
                restauranteId, inicioDoDia, fimDoDia, statusValidos
        );

        BigDecimal receitaTotal = pedidosDoDia.stream().map(Pedido::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucroTotal = pedidosDoDia.stream().map(Pedido::getLucro).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<PedidoFinanceiroDTO> pedidosDTO = pedidosDoDia.stream().map(PedidoFinanceiroDTO::new).collect(Collectors.toList());

        return new FinanceiroResponseDTO(receitaTotal, lucroTotal, pedidosDoDia.size(), pedidosDTO);
    }

    public FinanceiroMensalResponseDTO buscarDadosDoMes(UUID restauranteId) {
        validarAcessoRestaurante(restauranteId);
        YearMonth mesCorrente = YearMonth.now();
        LocalDateTime inicioDoMes = mesCorrente.atDay(1).atStartOfDay();
        LocalDateTime fimDoMes = mesCorrente.atEndOfMonth().atTime(23, 59, 59);
        List<Pedido.StatusPedido> statusValidos = List.of(Pedido.StatusPedido.CONCLUIDO, Pedido.StatusPedido.ENTREGUE);

        List<Pedido> pedidosDoMes = pedidoRepository.findByRestauranteIdAndDataPedidoBetweenAndStatusIn(
                restauranteId, inicioDoMes, fimDoMes, statusValidos
        );

        BigDecimal receitaTotal = pedidosDoMes.stream().map(Pedido::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucroTotal = pedidosDoMes.stream().map(Pedido::getLucro).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FinanceiroMensalResponseDTO(receitaTotal, lucroTotal);
    }

    // ✨ NOVO MÉTODO PARA BUSCAR O TOTAL DE PEDIDOS POR DIA NO MÊS
    public List<PedidosPorDiaDTO> buscarTotalPedidosPorDiaDoMes(UUID restauranteId) {
        validarAcessoRestaurante(restauranteId);

        YearMonth mesCorrente = YearMonth.now();
        LocalDateTime inicioDoMes = mesCorrente.atDay(1).atStartOfDay();
        LocalDateTime fimDoMes = mesCorrente.atEndOfMonth().atTime(23, 59, 59);
        List<Pedido.StatusPedido> statusValidos = List.of(Pedido.StatusPedido.CONCLUIDO, Pedido.StatusPedido.ENTREGUE);

        List<Pedido> pedidosDoMes = pedidoRepository.findByRestauranteIdAndDataPedidoBetweenAndStatusIn(
                restauranteId, inicioDoMes, fimDoMes, statusValidos
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        Map<LocalDate, Long> pedidosPorDiaMap = pedidosDoMes.stream()
                .collect(Collectors.groupingBy(
                        pedido -> pedido.getDataPedido().toLocalDate(),
                        Collectors.counting()
                ));

        return pedidosPorDiaMap.entrySet().stream()
                .map(entry -> new PedidosPorDiaDTO(
                        entry.getKey().format(formatter),
                        entry.getValue().intValue()
                ))
                .sorted(Comparator.comparing(PedidosPorDiaDTO::getDia))
                .collect(Collectors.toList());
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
        if (!usuarioLogado.getRestaurante().getId().equals(restauranteIdUrl)) {
            throw new SecurityException("Acesso negado: o usuário não tem permissão para acessar este restaurante.");
        }
    }
}
