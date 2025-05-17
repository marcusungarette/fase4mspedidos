package br.com.fiap.fase4mspedidos.client.impl;

import br.com.fiap.fase4mspedidos.client.InventoryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InventoryClientImpl implements InventoryClient {
    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl = "http://fase4msestoque-inventory-app-1:8083";

    public InventoryClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean checkAvailability(Long productId, Integer quantity) {
        String url = inventoryServiceUrl + "/inventory/check/" + productId + "/" + quantity;
        return restTemplate.getForObject(url, Boolean.class);
    }

    @Override
    public void reduceStock(Long productId, Integer quantity) {
        String url = inventoryServiceUrl + "/inventory/reduce/" + productId + "/" + quantity;
        restTemplate.postForObject(url, null, Void.class);
    }

    @Override
    public void restoreStock(Long productId, Integer quantity) {
        String url = inventoryServiceUrl + "/inventory/restore/" + productId + "/" + quantity;
        restTemplate.postForObject(url, null, Void.class);
    }
}