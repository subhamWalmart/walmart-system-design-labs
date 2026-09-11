package com.training.retailorderhub.paymentservice.service;

/**
 * Carried forward unchanged from the Day 2 monolith (OCP: one implementation
 * per payment method, PaymentService depends on this interface and a Map of
 * beans keyed by payment method name - never on if/else logic).
 */
public interface PaymentStrategy {
    boolean charge(double amount);
}
