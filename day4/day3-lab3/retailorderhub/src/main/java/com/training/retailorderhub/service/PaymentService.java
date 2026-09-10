package com.training.retailorderhub.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * TRAINING NOTE (Day 2):
 * - Extracted from OrderManager in the Day 2 demo (SRP): started as a single
 *   charge() method with an if/else chain.
 * - Rewritten here per Lab 2 (OCP): Spring collects every PaymentStrategy
 *   bean into this Map, keyed by the @Component name each strategy was given
 *   (CREDIT_CARD, PAYPAL, GIFT_CARD, APPLE_PAY, ...). Adding a payment method
 *   never requires touching this file again - see ApplePayStrategy.
 *
 * TRAINING NOTE (Day 3, Lab 4 - Circuit Breaker):
 * The whole charge(String, double) method is annotated - not just the
 * strategy lookup - because Spring's proxy-based AOP only intercepts calls
 * made from outside this class. Resilience4j calls chargeFallback() instead
 * of the real method body whenever the breaker is Open, or whenever the
 * wrapped call throws.
 */
@Service
public class PaymentService {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentService(Map<String, PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "chargeFallback")
    public boolean charge(String paymentMethod, double amount) {
        PaymentStrategy strategy = strategies.get(paymentMethod);
        if (strategy == null) {
            System.out.println("Unknown payment method: " + paymentMethod);
            throw new RuntimeException("Unknown payment method: " + paymentMethod);
        }
        return strategy.charge(amount);
    }

    private boolean chargeFallback(String paymentMethod, double amount, Throwable t) {
        System.out.println("Payment temporarily unavailable: " + t.getMessage());
        return false;
    }
}
