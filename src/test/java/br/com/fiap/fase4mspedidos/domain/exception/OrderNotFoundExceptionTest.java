package br.com.fiap.fase4mspedidos.domain.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderNotFoundExceptionTest {

    @Test
    void constructor_ShouldCreateCorrectMessage() {
        String orderId = "123";
        OrderNotFoundException exception = new OrderNotFoundException(orderId);

        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        assertNotNull(exception);
    }

    @Test
    void class_ShouldHaveNotFoundStatus() {
        ResponseStatus annotation = OrderNotFoundException.class.getAnnotation(ResponseStatus.class);

        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    @Test
    void exception_ShouldInheritFromRuntimeException() {
        OrderNotFoundException exception = new OrderNotFoundException("123");

        assertEquals(RuntimeException.class, exception.getClass().getSuperclass());
    }
}