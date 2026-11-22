package com.jdk.inventory.rates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

/** Testing an HTTP client without the real external service (and without Docker). */
class ExchangeRateClientTest {

    MockRestServiceServer server;
    ExchangeRateClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();          // intercepts requests made by this builder
        client = new ExchangeRateClient(builder, "https://rates.test");
    }

    @Test
    void convertsUsingTheRateFromTheApi() {
        server.expect(requestTo("https://rates.test/latest?base=USD&symbols=EGP"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"base\":\"USD\",\"rates\":{\"EGP\":48.50}}", MediaType.APPLICATION_JSON));

        assertThat(client.convert(new BigDecimal("10"), "EGP")).isEqualByComparingTo("485.00");
        server.verify();                                                  // every expected request happened
    }

    @Test
    void missingRateIsAnError() {
        server.expect(requestTo("https://rates.test/latest?base=USD&symbols=XYZ"))
                .andRespond(withSuccess("{\"base\":\"USD\",\"rates\":{}}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.convert(BigDecimal.ONE, "XYZ")).hasMessage("no rate for XYZ");
    }

    @Test
    void serverErrorsPropagate() {
        server.expect(requestTo("https://rates.test/latest?base=USD&symbols=EGP")).andRespond(withServiceUnavailable());

        assertThatThrownBy(() -> client.convert(BigDecimal.ONE, "EGP")).isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }
}
