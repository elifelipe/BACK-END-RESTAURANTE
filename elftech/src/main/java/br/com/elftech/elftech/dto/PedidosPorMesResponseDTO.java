package br.com.elftech.elftech.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidosPorMesResponseDTO {
    private String mes;
    private Long totalPedidos;
}
