package br.com.fiap.fase4mspedidos.client.impl;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    private CustomerClientImpl customerClient;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @BeforeEach
    void setUp() {
        customerClient = new CustomerClientImpl(restTemplate);
    }

    @Test
    void validateCustomer_ShouldReturnTrue_WhenCustomerExists() {
        Long customerId = 123L;
        String expectedUrl = "http://fase4msclientes-app-1:8081/customers/123";

        Object mockResponse = new Object();

        when(restTemplate.getForObject(anyString(), eq(Object.class)))
                .thenReturn(mockResponse);

        boolean result = customerClient.validateCustomer(customerId);

        verify(restTemplate).getForObject(urlCaptor.capture(), eq(Object.class));
        assertEquals(expectedUrl, urlCaptor.getValue());
        assertTrue(result);
    }

    @Test
    void validateCustomer_ShouldReturnFalse_WhenCustomerDoesNotExist() {
        Long customerId = 456L;

        when(restTemplate.getForObject(anyString(), eq(Object.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        boolean result = customerClient.validateCustomer(customerId);

        verify(restTemplate).getForObject(contains("/customers/456"), eq(Object.class));
        assertFalse(result);
    }

    @Test
    void validateCustomer_ShouldReturnFalse_WhenServerError() {
        Long customerId = 789L;

        when(restTemplate.getForObject(anyString(), eq(Object.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = customerClient.validateCustomer(customerId);

        verify(restTemplate).getForObject(contains("/customers/789"), eq(Object.class));
        assertFalse(result);
    }

    @Test
    void validateCustomer_ShouldReturnFalse_WhenNetworkError() {
        Long customerId = 101L;

        when(restTemplate.getForObject(anyString(), eq(Object.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        boolean result = customerClient.validateCustomer(customerId);

        verify(restTemplate).getForObject(contains("/customers/101"), eq(Object.class));
        assertFalse(result);
    }

    @Test
    void validateCustomer_ShouldReturnTrue_WhenResponseIsNull() {
        Long customerId = 202L;

        when(restTemplate.getForObject(anyString(), eq(Object.class)))
                .thenReturn(null);

        boolean result = customerClient.validateCustomer(customerId);

        verify(restTemplate).getForObject(contains("/customers/202"), eq(Object.class));
        assertTrue(result);
    }
}