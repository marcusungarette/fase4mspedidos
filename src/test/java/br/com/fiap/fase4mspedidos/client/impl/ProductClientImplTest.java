package br.com.fiap.fase4mspedidos.client.impl;

import br.com.fiap.fase4mspedidos.client.ProductClient;
import br.com.fiap.fase4mspedidos.domain.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    private ProductClientImpl productClient;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @BeforeEach
    void setUp() {
        productClient = new ProductClientImpl(restTemplate);
    }

    @Test
    void getProductBySku_ShouldReturnProduct_WhenProductExists() {
        String sku = "SKU-001";
        String expectedUrl = "http://fase4msprodutos-product-app-1:8082/products/sku/SKU-001";

        ProductClient.ProductResponse mockResponse = new ProductClient.ProductResponse(
                123L,
                "Test Product",
                sku,
                new BigDecimal("29.99")
        );

        when(restTemplate.getForObject(anyString(), eq(ProductClient.ProductResponse.class)))
                .thenReturn(mockResponse);

        ProductClient.ProductResponse result = productClient.getProductBySku(sku);

        verify(restTemplate).getForObject(urlCaptor.capture(), eq(ProductClient.ProductResponse.class));
        assertEquals(expectedUrl, urlCaptor.getValue());
        assertNotNull(result);
        assertEquals(mockResponse.getId(), result.getId());
        assertEquals(mockResponse.getName(), result.getName());
        assertEquals(mockResponse.getSku(), result.getSku());
        assertEquals(mockResponse.getPrice(), result.getPrice());
    }

    @Test
    void getProductBySku_ShouldThrowProductNotFoundException_WhenProductDoesNotExist() {
        String sku = "INVALID-SKU";

        when(restTemplate.getForObject(anyString(), eq(ProductClient.ProductResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));


        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productClient.getProductBySku(sku);
        });

        verify(restTemplate).getForObject(contains("/products/sku/INVALID-SKU"), eq(ProductClient.ProductResponse.class));
        assertTrue(exception.getMessage().contains(sku));
    }

    @Test
    void getProductBySku_ShouldThrowProductNotFoundException_WhenServerError() {
        String sku = "SERVER-ERROR";

        when(restTemplate.getForObject(anyString(), eq(ProductClient.ProductResponse.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productClient.getProductBySku(sku);
        });

        verify(restTemplate).getForObject(contains("/products/sku/SERVER-ERROR"), eq(ProductClient.ProductResponse.class));
        assertTrue(exception.getMessage().contains(sku));
    }

    @Test
    void getProductBySku_ShouldThrowProductNotFoundException_WhenNetworkError() {
        String sku = "NETWORK-ERROR";

        when(restTemplate.getForObject(anyString(), eq(ProductClient.ProductResponse.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productClient.getProductBySku(sku);
        });

        verify(restTemplate).getForObject(contains("/products/sku/NETWORK-ERROR"), eq(ProductClient.ProductResponse.class));
        assertTrue(exception.getMessage().contains(sku));
    }

}