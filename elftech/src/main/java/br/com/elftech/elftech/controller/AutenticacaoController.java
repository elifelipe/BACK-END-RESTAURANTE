package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.DadosAutenticacao;
import br.com.elftech.elftech.dto.DadosRegistro;
import br.com.elftech.elftech.model.Usuario;
import br.com.elftech.elftech.service.AutenticacaoService;
import br.com.elftech.elftech.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AutenticacaoService autenticacaoService;

    // DTO para a resposta do token, agora incluindo a 'role'
    record DadosTokenJWT(String token, UUID restauranteId, String role) {}

    @PostMapping("/login")
    public ResponseEntity efetuarLogin(@RequestBody DadosAutenticacao dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        var authentication = manager.authenticate(authenticationToken);

        // Pegamos o objeto completo do usuário que foi autenticado
        var usuario = (Usuario) authentication.getPrincipal();

        // Geramos o token
        var tokenJWT = tokenService.gerarToken(usuario);

        // --- RESPOSTA ATUALIZADA ---
        // Agora retornamos o token, o ID do restaurante E a role do usuário
        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT, usuario.getRestaurante().getId(), usuario.getRole()));
    }

    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity registrar(@RequestBody DadosRegistro dados) {
        autenticacaoService.criarUsuario(dados);
        return ResponseEntity.status(201).build();
    }
}