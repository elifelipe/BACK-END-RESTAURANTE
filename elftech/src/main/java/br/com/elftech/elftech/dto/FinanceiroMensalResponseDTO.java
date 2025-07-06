package br.com.elftech.elftech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceiroMensalResponseDTO {
    private BigDecimal receitaTotal;
    private BigDecimal lucroTotal;
}
