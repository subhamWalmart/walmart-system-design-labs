package com.training.retailorderhub.paymentservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Carried forward unchanged from the Day 2 monolith. Spring still collects
 * every PaymentStrategy bean into this Map, keyed by the @Component name
 * each strategy was given - the only thing that changed is that
 * PaymentController now calls charge() over HTTP instead of OrderService
 * calling it as a plain Java method.
 */
@Service
public class PaymentService {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentService(Map<String, PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public boolean charge(String paymentMethod, double amount) {
        PaymentStrategy strategy = strategies.get(paymentMethod);
        if (strategy == null) {
            System.out.println("Unknown payment method: " + paymentMethod);
            return false;
        }
        return strategy.charge(amount);
    }
}
