package br.com.elftech.elftech.dto;

import java.util.UUID;

public record DadosRegistro(String login, String senha, UUID restauranteId) {
}