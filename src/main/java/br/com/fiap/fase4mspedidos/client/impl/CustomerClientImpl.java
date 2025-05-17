package br.com.fiap.fase4mspedidos.client.impl;

import br.com.fiap.fase4mspedidos.client.CustomerClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerClientImpl implements CustomerClient {
    private final RestTemplate restTemplate;
    private final String customerServiceUrl = "http://fase4msclientes-app-1:8081";

    public CustomerClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean validateCustomer(Long customerId) {
        try {
            String url = customerServiceUrl + "/customers/" + customerId;
            Object response = restTemplate.getForObject(url, Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}