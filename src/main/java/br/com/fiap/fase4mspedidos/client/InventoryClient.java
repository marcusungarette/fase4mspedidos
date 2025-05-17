package br.com.fiap.fase4mspedidos.client;

public interface InventoryClient {
    boolean checkAvailability(Long productId, Integer quantity);
    void reduceStock(Long productId, Integer quantity);
    void restoreStock(Long productId, Integer quantity);
}