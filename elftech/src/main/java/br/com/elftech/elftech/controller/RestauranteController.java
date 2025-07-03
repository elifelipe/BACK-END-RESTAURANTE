package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.CriarRestauranteRequest;
import br.com.elftech.elftech.dto.RestauranteComUsuariosDTO;
import br.com.elftech.elftech.dto.RestauranteResponseDTO;
import br.com.elftech.elftech.model.Restaurante;
import br.com.elftech.elftech.service.RestauranteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurantes")
public class RestauranteController {

    @Autowired
    private RestauranteService restauranteService;

    @PostMapping
    public ResponseEntity<RestauranteResponseDTO> criarRestaurante(@RequestBody CriarRestauranteRequest request) {
        Restaurante restauranteCriado = restauranteService.criarRestaurante(request);
        URI location = URI.create("/api/restaurantes/" + restauranteCriado.getId());
        return ResponseEntity.created(location).body(new RestauranteResponseDTO(restauranteCriado));
    }

    @GetMapping
    public ResponseEntity<List<RestauranteComUsuariosDTO>> listarRestaurantes() {
        return ResponseEntity.ok(restauranteService.listarRestaurantes());
    }

    // --- MÉTODO ATUALIZADO E SIMPLIFICADO ---
    @GetMapping("/{restauranteId}")
    public ResponseEntity<RestauranteResponseDTO> buscarPorId(@PathVariable UUID restauranteId) {
        // O controller agora só repassa o DTO que o serviço já preparou
        RestauranteResponseDTO restauranteDTO = restauranteService.buscarPorId(restauranteId);
        return ResponseEntity.ok(restauranteDTO);
    }

    @DeleteMapping("/{restauranteId}")
    public ResponseEntity<Void> deletarRestaurante(@PathVariable UUID restauranteId) {
        restauranteService.deletarRestaurante(restauranteId);
        return ResponseEntity.noContent().build();
    }
}