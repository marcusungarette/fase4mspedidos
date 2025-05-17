package br.com.fiap.fase4mspedidos.client.impl;

import br.com.fiap.fase4mspedidos.client.PaymentClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    private PaymentClientImpl paymentClient;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<PaymentClient.PaymentRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        paymentClient = new PaymentClientImpl(restTemplate);
    }

    @Test
    void processPayment_ShouldAddCallbackUrlAndCallCorrectEndpoint() {
        String creditCardNumber = "4111111111111111";
        BigDecimal amount = new BigDecimal("100.00");
        String orderId = "123";
        PaymentClient.PaymentRequest request = new PaymentClient.PaymentRequest(creditCardNumber, amount, orderId, null);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", 456L);
        responseMap.put("externalId", "PAYER-789");
        responseMap.put("status", "PENDING");

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(responseMap);

        PaymentClient.PaymentResponse response = paymentClient.processPayment(request);

        verify(restTemplate).postForObject(urlCaptor.capture(), requestCaptor.capture(), eq(Map.class));

        String capturedUrl = urlCaptor.getValue();
        assertEquals("http://fase4mspagamento-payment-app-1:8085/payments", capturedUrl);

        PaymentClient.PaymentRequest capturedRequest = requestCaptor.getValue();
        assertEquals("http://fase4mspedidos-order-app-1:8084/payment-callbacks", capturedRequest.getCallbackUrl());
        assertEquals(creditCardNumber, capturedRequest.getCreditCardNumber());
        assertEquals(amount, capturedRequest.getAmount());
        assertEquals(orderId, capturedRequest.getOrderId());

        assertEquals(456L, response.getId());
        assertEquals("PAYER-789", response.getExternalId());
        assertTrue(response.isSuccess());
        assertEquals("Pagamento iniciado", response.getMessage());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void processPayment_ShouldHandleNullValues() {
        PaymentClient.PaymentRequest request = new PaymentClient.PaymentRequest(
                "4111111111111111", new BigDecimal("100.00"), "123", null);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", 456L);
        responseMap.put("externalId", null);
        responseMap.put("status", null);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(responseMap);

        PaymentClient.PaymentResponse response = paymentClient.processPayment(request);

        assertNull(response.getExternalId());
        assertNull(response.getStatus());
    }

    @Test
    void processPayment_ShouldHandleClientError() {
        PaymentClient.PaymentRequest request = new PaymentClient.PaymentRequest(
                "4111111111111111", new BigDecimal("100.00"), "123", null);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> {
            paymentClient.processPayment(request);
        });
    }

    @Test
    void processPayment_ShouldHandleServerError() {
        PaymentClient.PaymentRequest request = new PaymentClient.PaymentRequest(
                "4111111111111111", new BigDecimal("100.00"), "123", null);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> {
            paymentClient.processPayment(request);
        });
    }

    @Test
    void processPayment_ShouldHandleNetworkError() {
        PaymentClient.PaymentRequest request = new PaymentClient.PaymentRequest(
                "4111111111111111", new BigDecimal("100.00"), "123", null);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new ResourceAccessException("Network error"));
        assertThrows(ResourceAccessException.class, () -> {
            paymentClient.processPayment(request);
        });
    }


    @Test
    void checkPaymentStatus_ShouldCallCorrectEndpoint() {
        String paymentId = "PAYER-123";
        String expectedUrl = "http://fase4mspagamento-payment-app-1:8085/payments/PAYER-123";
        PaymentClient.PaymentStatusResponse mockResponse = new PaymentClient.PaymentStatusResponse();

        when(restTemplate.getForObject(anyString(), eq(PaymentClient.PaymentStatusResponse.class)))
                .thenReturn(mockResponse);

        PaymentClient.PaymentStatusResponse response = paymentClient.checkPaymentStatus(paymentId);

        verify(restTemplate).getForObject(urlCaptor.capture(), eq(PaymentClient.PaymentStatusResponse.class));
        assertEquals(expectedUrl, urlCaptor.getValue());
        assertEquals(mockResponse, response);
    }

    @Test
    void checkPaymentStatus_ShouldHandleClientError() {
        when(restTemplate.getForObject(anyString(), eq(PaymentClient.PaymentStatusResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.class, () -> {
            paymentClient.checkPaymentStatus("PAYER-123");
        });
    }

    @Test
    void checkPaymentStatus_ShouldHandleServerError() {
        when(restTemplate.getForObject(anyString(), eq(PaymentClient.PaymentStatusResponse.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> {
            paymentClient.checkPaymentStatus("PAYER-123");
        });
    }

    @Test
    void checkPaymentStatus_ShouldHandleNetworkError() {
        when(restTemplate.getForObject(anyString(), eq(PaymentClient.PaymentStatusResponse.class)))
                .thenThrow(new ResourceAccessException("Network error"));

        assertThrows(ResourceAccessException.class, () -> {
            paymentClient.checkPaymentStatus("PAYER-123");
        });
    }

    @Test
    void checkPaymentStatus_ShouldHandleNullResponse() {
        when(restTemplate.getForObject(anyString(), eq(PaymentClient.PaymentStatusResponse.class)))
                .thenReturn(null);

        PaymentClient.PaymentStatusResponse response = paymentClient.checkPaymentStatus("PAYER-123");

        assertNull(response);
    }
}