package br.com.fiap.fase4mspedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "br.com.fiap.fase4mspedidos.gateway.database.jpa.entity")
@EnableJpaRepositories(basePackages = "br.com.fiap.fase4mspedidos.gateway.database.jpa.repository")
public class Fase4mspedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(Fase4mspedidosApplication.class, args);
    }

}
