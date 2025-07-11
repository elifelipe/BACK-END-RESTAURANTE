package br.com.elftech.elftech.dto;

// Classe DTO para a requisição de pagamento PIX
public class PixPaymentRequest {
    private Double amount; // Valor da cobrança
    private String description; // Descrição da cobrança

    // Construtor padrão
    public PixPaymentRequest() {
    }

    // Construtor com todos os campos
    public PixPaymentRequest(Double amount, String description) {
        this.amount = amount;
        this.description = description;
    }

    // Getters e Setters
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

    @Override
    public String toString() {
        return "PixPaymentRequest{" +
                "amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}
