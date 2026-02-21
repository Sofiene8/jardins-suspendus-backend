package com.jardinssuspendus.config;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode; // "sandbox" ou "live"

    /**
     * Client HTTP PayPal injecté dans PaymentService.
     * Mode sandbox pour les tests, live pour la production.
     */
    @Bean
    public PayPalHttpClient payPalHttpClient() {
        PayPalEnvironment environment = "live".equalsIgnoreCase(mode)
                ? new PayPalEnvironment.Live(clientId, clientSecret)
                : new PayPalEnvironment.Sandbox(clientId, clientSecret);

        return new PayPalHttpClient(environment);
    }

    @Bean
    public String paypalClientId() {
        return clientId;
    }

    @Bean
    public String paypalMode() {
        return mode;
    }
}