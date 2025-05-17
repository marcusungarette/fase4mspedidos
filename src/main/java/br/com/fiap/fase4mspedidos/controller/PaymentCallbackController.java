package br.com.fiap.fase4mspedidos.controller;

import br.com.fiap.fase4mspedidos.client.PaymentClient.PaymentNotification;
import br.com.fiap.fase4mspedidos.usecase.ProcessPaymentCallbackUsecase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment-callbacks")
public class PaymentCallbackController {

    private final ProcessPaymentCallbackUsecase processPaymentCallbackUsecase;

    public PaymentCallbackController(ProcessPaymentCallbackUsecase processPaymentCallbackUsecase) {
        this.processPaymentCallbackUsecase = processPaymentCallbackUsecase;
    }

    @PostMapping
    public ResponseEntity<String> handlePaymentCallback(@RequestBody PaymentNotification notification) {
        processPaymentCallbackUsecase.execute(notification);
        return ResponseEntity.ok("Callback processado com sucesso");
    }
}