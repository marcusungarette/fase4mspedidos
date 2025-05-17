package br.com.fiap.fase4mspedidos.client.impl;

import br.com.fiap.fase4mspedidos.client.PaymentClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class PaymentClientImpl implements PaymentClient {
    private final RestTemplate restTemplate;
    private final String paymentServiceUrl = "http://fase4mspagamento-payment-app-1:8085";
    private final String callbackUrl = "http://fase4mspedidos-order-app-1:8084/payment-callbacks";

    public PaymentClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Adiciona a URL de callback ao request
        PaymentRequest requestWithCallback = new PaymentRequest(
                request.getCreditCardNumber(),
                request.getAmount(),
                request.getOrderId(),
                callbackUrl
        );

        // Faz a chamada para o serviço de pagamento
        String url = paymentServiceUrl + "/payments";
        Map<String, Object> responseBody = restTemplate.postForObject(
                url,
                requestWithCallback,
                Map.class
        );

        // Converte a resposta
        return new PaymentResponse(
                ((Number) responseBody.get("id")).longValue(),
                (String) responseBody.get("externalId"),
                true, // O processamento foi iniciado com sucesso
                "Pagamento iniciado",
                (String) responseBody.get("status")
        );
    }


    @Override
    public PaymentStatusResponse checkPaymentStatus(String paymentId) {
        String url = paymentServiceUrl + "/payments/" + paymentId;
        return restTemplate.getForObject(url, PaymentStatusResponse.class);
    }
}