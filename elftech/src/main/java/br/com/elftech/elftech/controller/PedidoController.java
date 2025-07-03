package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.CriarPedidoRequest;
import br.com.elftech.elftech.dto.PedidoResponseDTO;
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
     * A resposta é um DTO limpo, sem referências circulares.
     */
    @PostMapping("/public/pedidos")
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody CriarPedidoRequest dados) {
        Pedido novoPedidoEntidade = pedidoService.criarPedido(dados);
        // Converte a entidade salva para um DTO de resposta antes de enviar
        return ResponseEntity.status(HttpStatus.CREATED).body(new PedidoResponseDTO(novoPedidoEntidade));
    }

    /**
     * Endpoint PROTEGIDO para o admin de um restaurante listar seus pedidos.
     */
    @GetMapping("/restaurantes/{restauranteId}/pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos(@PathVariable UUID restauranteId) {
        // O serviço já retorna a lista de DTOs pronta para ser enviada
        List<PedidoResponseDTO> pedidos = pedidoService.listarPedidosPorRestaurante(restauranteId);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Endpoint PROTEGIDO para o admin marcar um pedido como concluído.
     */
    @PutMapping("/restaurantes/{restauranteId}/pedidos/{pedidoId}/concluir")
    public ResponseEntity<PedidoResponseDTO> concluirPedido(@PathVariable UUID restauranteId, @PathVariable UUID pedidoId) {
        // O serviço já retorna o DTO do pedido atualizado
        PedidoResponseDTO pedidoAtualizado = pedidoService.marcarComoConcluido(restauranteId, pedidoId);
        return ResponseEntity.ok(pedidoAtualizado);
    }
}