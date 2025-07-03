package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.CriarItemRequest;
import br.com.elftech.elftech.dto.ItemCardapioResponse; // Importe o DTO de resposta
import br.com.elftech.elftech.model.Restaurante;
import br.com.elftech.elftech.service.CardapioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurantes/{restauranteId}/cardapio")
public class CardapioController {

    @Autowired
    private CardapioService cardapioService;

    @GetMapping
    public ResponseEntity<List<ItemCardapioResponse>> listarItens(@PathVariable UUID restauranteId) {
        List<ItemCardapioResponse> itens = cardapioService.listarItensPorRestaurante(restauranteId);
        return ResponseEntity.ok(itens);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deletarItem(@PathVariable UUID restauranteId, @PathVariable UUID itemId) {
        cardapioService.deletarItem(restauranteId, itemId);
        return ResponseEntity.noContent().build();
    }

    // --- NOVO ENDPOINT ---
    @PutMapping("/{itemId}/disponibilidade")
    public ResponseEntity<Void> alternarDisponibilidade(@PathVariable UUID restauranteId, @PathVariable UUID itemId) {
        cardapioService.alternarDisponibilidade(restauranteId, itemId);
        return ResponseEntity.noContent().build();
    }

    // --- MUDANÇA PRINCIPAL AQUI ---
    // A resposta agora é um DTO
    @PostMapping
    public ResponseEntity<ItemCardapioResponse> criarItem(@PathVariable UUID restauranteId, @RequestBody CriarItemRequest request) {
        Restaurante restaurante = new Restaurante();
        restaurante.setId(restauranteId);

        ItemCardapioResponse novoItemDto = cardapioService.criarItem(request, restaurante);
        return ResponseEntity.status(201).body(novoItemDto);
    }
}