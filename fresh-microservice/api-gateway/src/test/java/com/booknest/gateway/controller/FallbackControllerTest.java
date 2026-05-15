package com.booknest.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testFallback() {
        webTestClient.get().uri("/fallback")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Unavailable");
    }
}
