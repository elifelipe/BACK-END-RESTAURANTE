package br.com.elftech.elftech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidosPorDiaDTO {
    private String dia;
    private int totalPedidos;
}
