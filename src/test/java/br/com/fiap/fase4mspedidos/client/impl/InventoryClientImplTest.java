package br.com.fiap.fase4mspedidos.client.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    private InventoryClientImpl inventoryClient;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @BeforeEach
    void setUp() {
        inventoryClient = new InventoryClientImpl(restTemplate);
    }

    @Test
    void checkAvailability_ShouldCallCorrectUrl() {
        // Arrange
        Long productId = 123L;
        Integer quantity = 5;
        String expectedUrl = "http://fase4msestoque-inventory-app-1:8083/inventory/check/123/5";
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);

        // Act
        boolean result = inventoryClient.checkAvailability(productId, quantity);

        // Assert
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(Boolean.class));
        assertEquals(expectedUrl, urlCaptor.getValue());
        assertTrue(result);
    }

    @Test
    void reduceStock_ShouldCallCorrectUrl() {
        // Arrange
        Long productId = 123L;
        Integer quantity = 5;
        String expectedUrl = "http://fase4msestoque-inventory-app-1:8083/inventory/reduce/123/5";

        // Act
        inventoryClient.reduceStock(productId, quantity);

        // Assert
        verify(restTemplate).postForObject(urlCaptor.capture(), isNull(), eq(Void.class));
        assertEquals(expectedUrl, urlCaptor.getValue());
    }

    @Test
    void restoreStock_ShouldCallCorrectUrl() {
        // Arrange
        Long productId = 123L;
        Integer quantity = 5;
        String expectedUrl = "http://fase4msestoque-inventory-app-1:8083/inventory/restore/123/5";

        // Act
        inventoryClient.restoreStock(productId, quantity);

        // Assert
        verify(restTemplate).postForObject(urlCaptor.capture(), isNull(), eq(Void.class));
        assertEquals(expectedUrl, urlCaptor.getValue());
    }

}