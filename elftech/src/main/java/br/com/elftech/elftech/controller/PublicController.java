package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.ItemCardapioResponse;
import br.com.elftech.elftech.dto.RestauranteResponseDTO;
import br.com.elftech.elftech.dto.StatusPedidoResponseDTO;
import br.com.elftech.elftech.service.CardapioService;
import br.com.elftech.elftech.service.PedidoService;
import br.com.elftech.elftech.service.RestauranteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public") // Caminho base genérico para todas as rotas públicas
public class PublicController {

    @Autowired
    private RestauranteService restauranteService;

    @Autowired
    private CardapioService cardapioService;

    @Autowired
    private PedidoService pedidoService;

    /**
     * Endpoint público para buscar o status atual de um pedido.
     * URL: GET /api/public/pedidos/{pedidoId}/status
     */
    @GetMapping("/pedidos/{pedidoId}/status")
    public ResponseEntity<StatusPedidoResponseDTO> getStatusPedido(@PathVariable UUID pedidoId) {
        StatusPedidoResponseDTO statusDTO = pedidoService.getStatusPedido(pedidoId);
        return ResponseEntity.ok(statusDTO);
    }

    /**
     * Endpoint público para buscar os dados de um restaurante.
     * URL: GET /api/public/restaurantes/{id}
     */
    @GetMapping("/restaurantes/{id}")
    public ResponseEntity<RestauranteResponseDTO> buscarRestaurante(@PathVariable UUID id) {
        // Chamamos o método PÚBLICO que não tem a trava de segurança
        RestauranteResponseDTO restauranteDTO = restauranteService.buscarPublicoPorId(id);
        return ResponseEntity.ok(restauranteDTO);
    }

    /**
     * Endpoint público para buscar o cardápio de um restaurante.
     * URL: GET /api/public/restaurantes/{id}/cardapio
     */
    @GetMapping("/restaurantes/{id}/cardapio")
    public ResponseEntity<List<ItemCardapioResponse>> buscarCardapio(@PathVariable UUID id) {
        var cardapio = cardapioService.listarItensPorRestaurante(id);
        return ResponseEntity.ok(cardapio);
    }
}