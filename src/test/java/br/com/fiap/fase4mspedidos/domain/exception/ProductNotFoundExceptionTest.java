package br.com.fiap.fase4mspedidos.domain.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProductNotFoundExceptionTest {

    @Test
    void constructor_ShouldCreateCorrectMessage() {
        String sku = "ABC123";
        ProductNotFoundException exception = new ProductNotFoundException(sku);

        assertEquals("Product not found with SKU: " + sku, exception.getMessage());
        assertNotNull(exception);
    }

    @Test
    void class_ShouldHaveNotFoundStatus() {
        ResponseStatus annotation = ProductNotFoundException.class.getAnnotation(ResponseStatus.class);

        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    @Test
    void exception_ShouldInheritFromRuntimeException() {
        ProductNotFoundException exception = new ProductNotFoundException("ABC123");

        assertEquals(RuntimeException.class, exception.getClass().getSuperclass());
    }
}