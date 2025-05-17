package br.com.fiap.fase4mspedidos.client;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentClient {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentStatusResponse checkPaymentStatus(String paymentId);

    @Getter
    @AllArgsConstructor
    class PaymentRequest {
        private String creditCardNumber;
        private BigDecimal amount;
        private String orderId;
        private String callbackUrl;
    }

    @Getter
    @AllArgsConstructor
    class PaymentResponse {
        private Long id;
        private String externalId;
        private boolean success;
        private String message;
        private String status;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    class PaymentStatusResponse {
        private Long id;
        private String externalId;
        private String status;
        private String message;
        private BigDecimal amount;
        private String orderId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    class PaymentNotification {
        private Long paymentId;
        private String externalId;
        private String status;
        private String message;
        private String orderId;
    }
}