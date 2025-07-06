package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.FinanceiroMensalResponseDTO;
import br.com.elftech.elftech.dto.FinanceiroResponseDTO;
import br.com.elftech.elftech.dto.PedidosPorDiaDTO;
import br.com.elftech.elftech.service.FinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurantes")
public class FinanceiroController {

    @Autowired
    private FinanceiroService financeiroService;

    @GetMapping("/{restauranteId}/financeiro")
    public ResponseEntity<FinanceiroResponseDTO> getFinanceiroPorData(
            @PathVariable UUID restauranteId,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        FinanceiroResponseDTO dadosFinanceiros = financeiroService.buscarDados(restauranteId, data);
        return ResponseEntity.ok(dadosFinanceiros);
    }

    @GetMapping("/{restauranteId}/financeiro/mes")
    public ResponseEntity<FinanceiroMensalResponseDTO> getFinanceiroDoMes(@PathVariable UUID restauranteId) {
        FinanceiroMensalResponseDTO dadosMensais = financeiroService.buscarDadosDoMes(restauranteId);
        return ResponseEntity.ok(dadosMensais);
    }

    @GetMapping("/{restauranteId}/financeiro/pedidos-por-dia")
    public ResponseEntity<List<PedidosPorDiaDTO>> getTotalPedidosPorDia(@PathVariable UUID restauranteId) {
        List<PedidosPorDiaDTO> dados = financeiroService.buscarTotalPedidosPorDiaDoMes(restauranteId);
        return ResponseEntity.ok(dados);
    }
}
