package br.com.elftech.elftech.dto;

import lombok.Data;

@Data
public class CriarRestauranteRequest {
    // Dados do Restaurante
    private String nome;
    private String cnpj;

    // Dados do primeiro Usuário (o administrador)
    private String adminLogin;
    private String adminSenha;
}