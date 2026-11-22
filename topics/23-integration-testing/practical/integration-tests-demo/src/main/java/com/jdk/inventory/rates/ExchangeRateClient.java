package com.jdk.inventory.rates;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls an external HTTP API. Tested WITHOUT the real API using MockRestServiceServer. */
@Component
public class ExchangeRateClient {

    record RatesResponse(String base, Map<String, BigDecimal> rates) { }

    private final RestClient http;

    public ExchangeRateClient(RestClient.Builder builder, @Value("${rates.base-url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public BigDecimal convert(BigDecimal usd, String currency) {
        RatesResponse response = http.get()
                .uri(uri -> uri.path("/latest").queryParam("base", "USD").queryParam("symbols", currency).build())
                .retrieve()
                .body(RatesResponse.class);
        BigDecimal rate = response == null ? null : response.rates().get(currency);
        if (rate == null) {
            throw new IllegalStateException("no rate for " + currency);
        }
        return usd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
