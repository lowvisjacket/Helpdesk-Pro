package org.dev.ticketing.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GatewayConfigurationTest {
    private final GatewayConfiguration configuration = new GatewayConfiguration();

    @Test
    void usesTheClientAddressAsRateLimitKey() {
        var request = MockServerHttpRequest.get("/")
                .remoteAddress(new InetSocketAddress("192.0.2.10", 12345))
                .build();
        var exchange = MockServerWebExchange.from(request);

        assertEquals("192.0.2.10", configuration.remoteAddressKeyResolver().resolve(exchange).block());
    }

    @Test
    void rejectsRequestsWithoutAClientAddress() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());

        assertThrows(
                IllegalStateException.class,
                () -> configuration.remoteAddressKeyResolver().resolve(exchange).block()
        );
    }
}
