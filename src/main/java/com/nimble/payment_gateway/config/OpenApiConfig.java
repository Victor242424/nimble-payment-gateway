package com.nimble.payment_gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Payment Gateway API",
                version = "1.0.0",
                description = "API de Gateway de Pagamentos Simplificado - Desafio Nimble",
                contact = @Contact(
                        name = "Victor Rivas",
                        url = "https://github.com/usuario"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local"),
                @Server(url = "http://localhost:8080", description = "Production")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}