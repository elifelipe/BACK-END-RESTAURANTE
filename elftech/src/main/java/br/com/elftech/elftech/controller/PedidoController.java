package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.CriarPedidoRequest;
import br.com.elftech.elftech.dto.PedidoResponseDTO;
import br.com.elftech.elftech.dto.PedidosPorMesResponseDTO; // Importe o novo DTO
import br.com.elftech.elftech.model.Pedido;
import br.com.elftech.elftech.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api") // Prefixo base para as rotas
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    /**
     * Endpoint PÚBLICO para um cliente criar um novo pedido.
     */
    @PostMapping("/public/pedidos")
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody CriarPedidoRequest dados) {
        Pedido novoPedidoEntidade = pedidoService.criarPedido(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PedidoResponseDTO(novoPedidoEntidade));
    }

    /**
     * Endpoint PROTEGIDO para o admin de um restaurante listar seus pedidos.
     */
    @GetMapping("/restaurantes/{restauranteId}/pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos(@PathVariable UUID restauranteId) {
        List<PedidoResponseDTO> pedidos = pedidoService.listarPedidosPorRestaurante(restauranteId);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Endpoint PROTEGIDO para o admin marcar um pedido como concluído (pronto para entrega).
     */
    @PutMapping("/restaurantes/{restauranteId}/pedidos/{pedidoId}/concluir")
    public ResponseEntity<PedidoResponseDTO> concluirPedido(@PathVariable UUID restauranteId, @PathVariable UUID pedidoId) {
        PedidoResponseDTO pedidoAtualizado = pedidoService.marcarComoConcluido(restauranteId, pedidoId);
        return ResponseEntity.ok(pedidoAtualizado);
    }

    /**
     * NOVO ENDPOINT: Marca um pedido como ENTREGUE.
     * Acessado pelo Painel do Garçom.
     */
    @PutMapping("/restaurantes/{restauranteId}/pedidos/{pedidoId}/entregar")
    public ResponseEntity<PedidoResponseDTO> entregarPedido(@PathVariable UUID restauranteId, @PathVariable UUID pedidoId) {
        PedidoResponseDTO pedidoAtualizado = pedidoService.marcarComoEntregue(restauranteId, pedidoId);
        return ResponseEntity.ok(pedidoAtualizado);
    }

    /**
     * NOVO ENDPOINT: Retorna a evolução dos pedidos do ano por mês para um restaurante.
     * Corresponde ao endpoint que o frontend está buscando.
     * Rota: /api/restaurantes/{restauranteId}/financeiro/pedidos-por-mes?ano={ano}
     */
    @GetMapping("/restaurantes/{restauranteId}/financeiro/pedidos-por-mes")
    public ResponseEntity<List<PedidosPorMesResponseDTO>> getPedidosPorMesDoAno(
            @PathVariable UUID restauranteId,
            @RequestParam int ano) {
        List<PedidosPorMesResponseDTO> dadosAnuais = pedidoService.getPedidosPorMesDoAno(restauranteId, ano);
        return ResponseEntity.ok(dadosAnuais);
    }
}
