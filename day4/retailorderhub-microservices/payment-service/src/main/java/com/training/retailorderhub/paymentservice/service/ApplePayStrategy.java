package com.training.retailorderhub.paymentservice.service;

import org.springframework.stereotype.Component;

/**
 * TRAINING NOTE (Day 2, Lab 2, Step 4):
 * Added after PaymentService was already rewritten to use the strategy map.
 * Nothing in PaymentService.java changed to support this class - that's the
 * whole point of the Open-Closed exercise, and it's still true now that
 * PaymentService lives in its own microservice.
 */
@Component("APPLE_PAY")
public class ApplePayStrategy implements PaymentStrategy {

    @Override
    public boolean charge(double amount) {
        System.out.println("Charging Apple Pay: " + amount);
        return true;
    }
}
