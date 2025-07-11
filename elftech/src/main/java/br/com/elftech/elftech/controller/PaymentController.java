package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.dto.CardPaymentRequest;
import br.com.elftech.elftech.dto.PixPaymentRequest;
import br.com.elftech.elftech.service.EfiService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final EfiService efiService;

    // Injeção de dependência do EfiService
    public PaymentController(EfiService efiService) {
        this.efiService = efiService;
    }

    /**
     * Endpoint para criar uma cobrança PIX.
     * Recebe os dados da requisição do frontend e delega para o EfiService.
     * @param request Objeto contendo o valor e a descrição da cobrança PIX.
     * @return Mono<ResponseEntity<JsonNode>> com a resposta da Efí.
     */
    @PostMapping("/pix")
    public Mono<ResponseEntity<JsonNode>> createPixPayment(@RequestBody PixPaymentRequest request) {
        System.out.println("Recebida requisição para criar PIX. Valor: " + request.getAmount() + ", Descrição: " + request.getDescription());
        return efiService.createPixCharge(request.getAmount(), request.getDescription())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build())
                .doOnError(e -> System.err.println("Erro no controller ao criar PIX: " + e.getMessage()));
    }

    /**
     * Endpoint para criar uma cobrança com Cartão de Crédito.
     * Recebe o token do cartão (já seguro) e outros dados do frontend.
     * @param request Objeto contendo o token do cartão, valor, descrição e parcelas.
     * @return Mono<ResponseEntity<JsonNode>> com a resposta da Efí.
     */
    @PostMapping("/card")
    public Mono<ResponseEntity<JsonNode>> createCardPayment(@RequestBody CardPaymentRequest request) {
        System.out.println("Recebida requisição para criar pagamento com Cartão. Token: " + request.getPaymentToken() + ", Valor: " + request.getAmount());
        return efiService.createCardCharge(request.getPaymentToken(), request.getAmount(), request.getDescription(), request.getInstallments())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build())
                .doOnError(e -> System.err.println("Erro no controller ao criar pagamento com cartão: " + e.getMessage()));
    }

    /**
     * Endpoint para receber webhooks da Efí Bank.
     * Este endpoint deve ser configurado no painel da Efí para receber notificações.
     * @param webhookPayload O payload JSON enviado pela Efí.
     * @return ResponseEntity<String> de sucesso.
     */
    @PostMapping("/webhook/efi")
    public ResponseEntity<String> receiveEfiWebhook(@RequestBody JsonNode webhookPayload) {
        System.out.println("Webhook da Efí recebido!");
        efiService.handleWebhook(webhookPayload);
        return ResponseEntity.ok("Webhook recebido com sucesso!");
    }
}
