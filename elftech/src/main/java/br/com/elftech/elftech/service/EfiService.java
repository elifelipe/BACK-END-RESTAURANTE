package br.com.elftech.elftech.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class EfiService {

    @Value("${efi.client-id}")
    private String clientId;

    @Value("${efi.client-secret}")
    private String clientSecret;

    @Value("${efi.base-url}")
    private String baseUrl;

    @Value("${efi.pix-cert-path}")
    private String pixCertPath;

    @Value("${efi.pix-cert-password}")
    private String pixCertPassword;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourceLoader resourceLoader;

    // Construtor para injetar o ResourceLoader
    public EfiService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        // WebClient inicializado sem configuração SSL específica por padrão.
        // Para requisições PIX, um WebClient com SSL será criado dinamicamente.
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Retorna um WebClient configurado com SSL para requisições PIX.
     * Este método carrega o certificado PKCS12 fornecido pela Efí.
     * ATENÇÃO: O TrustManager configurado aqui (InsecureTrustManagerFactory)
     * é APENAS PARA TESTES e não deve ser usado em PRODUÇÃO.
     * Em produção, configure um TrustManager que valide a cadeia de certificados da Efí.
     */
    private WebClient getPixWebClient() {
        try {
            // Carrega o KeyStore (certificado PKCS12)
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            Resource resource = resourceLoader.getResource(pixCertPath);
            System.out.println("Tentando carregar certificado PIX de: " + resource.getDescription());
            try (InputStream certInputStream = resource.getInputStream()) {
                keyStore.load(certInputStream, pixCertPassword.toCharArray());
                System.out.println("Certificado PIX carregado com sucesso.");
            } catch (IOException | GeneralSecurityException e) {
                System.err.println("Erro ao carregar certificado PIX. Verifique o caminho e a senha: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Falha ao carregar certificado PIX. Verifique o caminho e a senha.", e);
            }

            // Inicializa o KeyManagerFactory com o KeyStore
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, pixCertPassword.toCharArray());
            System.out.println("KeyManagerFactory inicializado.");

            // Configura o HttpClient para usar o SSLContext
            // AQUI ESTÁ A CORREÇÃO: Construímos o io.netty.handler.ssl.SslContext diretamente
            // usando SslContextBuilder e o passamos para sslContextSpec.sslContext().
            io.netty.handler.ssl.SslContext nettySslContext = SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE) // APENAS PARA TESTES!
                    .build(); // Este é o ponto onde o SSLException pode ocorrer

            System.out.println("Netty SslContext construído com sucesso.");

            HttpClient httpClient = HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(nettySslContext));

            // Retorna um WebClient com o HttpClient configurado para SSL
            return WebClient.builder()
                    .baseUrl(baseUrl)
                    .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
                    .build();

        } catch (SSLException e) { // Captura especificamente SSLException
            System.err.println("Erro de SSL ao configurar o WebClient para PIX: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro de SSL durante a configuração do PIX: " + e.getMessage(), e);
        } catch (Exception e) { // Captura outras exceções inesperadas
            System.err.println("Erro geral inesperado ao configurar SSL para o PIX: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado durante a configuração SSL para o PIX: " + e.getMessage(), e);
        }
    }

    /**
     * Obtém o token de acesso da API da Efí Bank.
     * Este token é necessário para autenticar todas as outras requisições.
     * @return Mono<String> contendo o token de acesso.
     */
    private Mono<String> getAccessToken() {
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        return webClient.post()
                .uri("/oauth/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"grant_type\": \"client_credentials\"}")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> jsonNode.get("access_token").asText())
                .doOnError(e -> System.err.println("Erro ao obter token de acesso: " + e.getMessage()));
    }

    /**
     * Cria uma cobrança PIX na Efí Bank.
     * @param amount O valor da cobrança.
     * @param description A descrição da cobrança.
     * @return Mono<JsonNode> contendo a resposta da API da Efí (incluindo QR Code e BR Code).
     */
    public Mono<JsonNode> createPixCharge(Double amount, String description) {
        return getAccessToken().flatMap(accessToken -> {
            Map<String, Object> body = new HashMap<>();
            // Define o tempo de expiração da cobrança PIX em segundos (1 hora = 3600 segundos)
            body.put("calendario", Map.of("expiracao", 3600));
            // Dados do pagador (exemplo para testes - ajuste para dados reais do seu cliente)
            body.put("devedor", Map.of("cpf", "999.999.999-99", "nome", "Pagador Teste"));
            // Valor original da cobrança, formatado para duas casas decimais
            body.put("valor", Map.of("original", String.format("%.2f", amount)));
            // Sua chave PIX cadastrada na Efí (ex: CPF, CNPJ, e-mail, telefone, chave aleatória)
            body.put("chave", "71186353-2647-4643-9102-f4721a375a13"); // SUBSTITUA PELA SUA CHAVE PIX REAL DE TESTES
            // Mensagem que aparecerá para o pagador no aplicativo do banco
            body.put("solicitacaoPagador", description);

            return getPixWebClient().post() // Usa o WebClient com SSL para PIX
                    .uri("/v2/cob") // Endpoint para criação de cobrança PIX
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .doOnError(e -> System.err.println("Erro ao criar cobrança PIX: " + e.getMessage()));
        });
    }

    /**
     * Cria uma cobrança com Cartão de Crédito na Efí Bank.
     * @param paymentToken O token do cartão obtido no frontend via SDK da Efí.
     * @param amount O valor da cobrança.
     * @param description A descrição da cobrança.
     * @param installments O número de parcelas.
     * @return Mono<JsonNode> contendo a resposta da API da Efí sobre o processamento do cartão.
     */
    public Mono<JsonNode> createCardCharge(String paymentToken, Double amount, String description, Integer installments) {
        return getAccessToken().flatMap(accessToken -> {
            Map<String, Object> body = new HashMap<>();
            // Itens da cobrança (exemplo)
            body.put("items", new Object[]{Map.of(
                    "name", description,
                    "value", (int) (amount * 100), // Valor em centavos (inteiro)
                    "amount", 1
            )});
            // Informações de frete (opcional, exemplo)
            body.put("shippings", new Object[]{Map.of(
                    "name", "Default",
                    "value", 0 // Exemplo de frete
            )});

            // Dados do pagador (exemplo para testes - ajuste para dados reais do seu cliente)
            Map<String, Object> customer = new HashMap<>();
            customer.put("name", "Cliente de Teste");
            customer.put("cpf", "999.999.999-99"); // CPF de teste válido ou real do cliente
            customer.put("email", "cliente@test.com");
            customer.put("birth_date", "1990-01-01");
            customer.put("phone_number", "99999999999");
            customer.put("address", Map.of(
                    "street", "Rua de Teste",
                    "number", "123",
                    "neighborhood", "Bairro Teste",
                    "zipcode", "99999999",
                    "city", "Cidade Teste",
                    "state", "SP"
            ));
            body.put("payer", customer);

            // Dados do pagamento com cartão de crédito
            body.put("payment", Map.of(
                    "credit_card", Map.of(
                            "payment_token", paymentToken, // Token do cartão obtido no frontend
                            "installments", installments,
                            "billing_address", Map.of( // Endereço de cobrança do cartão (geralmente o mesmo do cliente)
                                    "street", "Rua de Teste",
                                    "number", "123",
                                    "neighborhood", "Bairro Teste",
                                    "zipcode", "99999999",
                                    "city", "Cidade Teste",
                                    "state", "SP"
                            )
                    )
            ));

            return webClient.post()
                    .uri("/v1/charge/one-step") // Endpoint para criação de cobrança de cartão de crédito
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .doOnError(e -> System.err.println("Erro ao criar cobrança com cartão: " + e.getMessage()));
        });
    }

    /**
     * Método para lidar com webhooks recebidos da Efí Bank.
     * Este método deve ser chamado pelo seu controlador quando um webhook for recebido.
     * @param webhookPayload O payload JSON do webhook.
     */
    public void handleWebhook(JsonNode webhookPayload) {
        // Exemplo de como processar o payload do webhook
        // Os campos exatos podem variar, consulte a documentação da Efí para webhooks.
        try {
            String notification = webhookPayload.has("notificacao") ? webhookPayload.get("notificacao").asText() : "N/A";
            JsonNode dataNode = webhookPayload.get("data");

            if (dataNode != null && dataNode.isArray() && dataNode.size() > 0) {
                JsonNode firstDataItem = dataNode.get(0);
                String chargeId = firstDataItem.has("charge_id") ? firstDataItem.get("charge_id").asText() : "N/A";
                String status = firstDataItem.has("status") ? firstDataItem.get("status").asText() : "N/A";

                System.out.println("Webhook Recebido! Notificação: " + notification + ", Charge ID: " + chargeId + ", Status: " + status);

                // ATUALIZE O STATUS DA COBRANÇA EM SEU BANCO DE DADOS AQUI!
                // Ex: paymentRepository.updatePaymentStatus(chargeId, status);
                // Lembre-se de validar a assinatura do webhook em um ambiente de produção para segurança.
            } else {
                System.out.println("Webhook recebido com estrutura de dados inesperada: " + webhookPayload.toPrettyString());
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
