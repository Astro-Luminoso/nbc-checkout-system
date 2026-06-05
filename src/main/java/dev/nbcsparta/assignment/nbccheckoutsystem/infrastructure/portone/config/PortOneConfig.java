package dev.nbcsparta.assignment.nbccheckoutsystem.infrastructure.portone.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PortOneConfig {

    @Value("${portone.base-url}")
    private String baseUrl;

    @Value("${portone.api-secret}")
    private String apiSecret;

    @Bean
    public RestClient portOneRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "PortOne " + apiSecret)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
