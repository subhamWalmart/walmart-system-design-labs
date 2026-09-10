package com.training.retailorderhub.service;

import org.springframework.stereotype.Component;

/**
 * TRAINING NOTE (Day 3, Lab 4 - Circuit Breaker):
 * A payment method that always fails, so the paymentGateway circuit breaker
 * can be tripped on demand without touching any of the real payment
 * strategies. Select "Simulate Failure (testing)" in the UI to use it.
 */
@Component("SIMULATE_FAILURE")
public class FailingPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean charge(double amount) {
        throw new RuntimeException("Simulated payment gateway outage");
    }
}
