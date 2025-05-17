package br.com.fiap.fase4mspedidos.client.impl;

import br.com.fiap.fase4mspedidos.client.ProductClient;
import br.com.fiap.fase4mspedidos.domain.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductClientImpl implements ProductClient {
    private final RestTemplate restTemplate;
    private final String productsServiceUrl = "http://fase4msprodutos-product-app-1:8082";

    public ProductClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ProductResponse getProductBySku(String sku) {
        try {
            String url = productsServiceUrl + "/products/sku/" + sku;
            return restTemplate.getForObject(url, ProductResponse.class);
        } catch (Exception e) {
            throw new ProductNotFoundException(sku);
        }
    }
}