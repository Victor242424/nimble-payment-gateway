package com.nimble.payment_gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizerService {

    private final WebClient.Builder webClientBuilder;

    @Value("${authorizer.url}")
    private String authorizerUrl;

    @Value("${authorizer.timeout:5000}")
    private long timeout;

    public boolean authorize() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(authorizerUrl).build();

            String response = webClient.get()
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("Erro ao consultar autorizador: Status {}, Body: {}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.just("DENIED");
                    })
                    .onErrorResume(Exception.class, ex -> {
                        log.error("Erro ao consultar autorizador", ex);
                        return Mono.just("DENIED");
                    })
                    .block();

            boolean authorized = response != null &&
                    (response.contains("\"status\":\"success\"") ||
                            response.contains("success"));

            log.info("Resposta do autorizador: {} - Autorizado: {}", response, authorized);
            return authorized;

        } catch (Exception e) {
            log.error("Erro inesperado ao consultar autorizador", e);
            return false;
        }
    }
}