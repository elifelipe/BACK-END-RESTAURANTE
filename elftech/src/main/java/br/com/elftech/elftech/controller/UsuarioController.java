package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.DadosListagemUsuario;
import br.com.elftech.elftech.service.AutenticacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios") // Novo endpoint base para gerenciamento de usuários
public class UsuarioController {

    @Autowired
    private AutenticacaoService autenticacaoService; // Usando o mesmo service por enquanto

    // Endpoint para LER todos os usuários
    @GetMapping
    public ResponseEntity<List<DadosListagemUsuario>> listarTodos() {
        var usuarios = autenticacaoService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // Endpoint para EXCLUIR um usuário por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable UUID id) {
        autenticacaoService.deletarUsuario(id);
        // Retorna 204 No Content, o padrão para exclusões bem-sucedidas
        return ResponseEntity.noContent().build();
    }
}