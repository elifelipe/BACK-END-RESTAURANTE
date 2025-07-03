package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.CategoriaRequest;
import br.com.elftech.elftech.dto.CategoriaResponse;
import br.com.elftech.elftech.model.Categoria;
import br.com.elftech.elftech.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurantes/{restauranteId}/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponse> criarCategoria(@PathVariable UUID restauranteId, @RequestBody CategoriaRequest request) {
        Categoria categoriaCriada = categoriaService.criarCategoria(restauranteId, request);
        URI location = URI.create(String.format("/api/restaurantes/%s/categorias/%s", restauranteId, categoriaCriada.getId()));
        return ResponseEntity.created(location).body(new CategoriaResponse(categoriaCriada));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarCategorias(@PathVariable UUID restauranteId) {
        List<CategoriaResponse> categorias = categoriaService.listarCategoriasPorRestaurante(restauranteId);
        return ResponseEntity.ok(categorias);
    }

    @PutMapping("/{categoriaId}")
    public ResponseEntity<CategoriaResponse> atualizarCategoria(@PathVariable UUID restauranteId, @PathVariable UUID categoriaId, @RequestBody CategoriaRequest request) {
        // Agora passamos o restauranteId para o serviço
        CategoriaResponse categoriaAtualizada = categoriaService.atualizarCategoria(restauranteId, categoriaId, request);
        return ResponseEntity.ok(categoriaAtualizada);
    }

    @DeleteMapping("/{categoriaId}")
    public ResponseEntity<Void> deletarCategoria(@PathVariable UUID restauranteId, @PathVariable UUID categoriaId) {
        // Agora passamos o restauranteId para o serviço
        categoriaService.deletarCategoria(restauranteId, categoriaId);
        return ResponseEntity.noContent().build();
    }
}