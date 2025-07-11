package br.com.elftech.elftech.dto;

// Classe DTO para a requisição de pagamento com Cartão de Crédito
public class CardPaymentRequest {
    private String paymentToken; // Token do cartão gerado no frontend (seguro)
    private Double amount; // Valor da cobrança
    private String description; // Descrição da cobrança
    private Integer installments; // Número de parcelas

    // Construtor padrão
    public CardPaymentRequest() {
    }

    // Construtor com todos os campos
    public CardPaymentRequest(String paymentToken, Double amount, String description, Integer installments) {
        this.paymentToken = paymentToken;
        this.amount = amount;
        this.description = description;
        this.installments = installments;
    }

    // Getters e Setters
    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }

    @Override
    public String toString() {
        return "CardPaymentRequest{" +
                "paymentToken='" + paymentToken + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", installments=" + installments +
                '}';
    }
}
