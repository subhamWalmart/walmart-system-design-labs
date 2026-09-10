package com.training.retailorderhub.service;

/**
 * TRAINING NOTE (Day 2, Lab 2 - Open-Closed Principle):
 * One implementation per payment method. PaymentService depends on this
 * interface and a Map of beans keyed by payment method name - never on
 * if/else logic. Adding a new payment method means adding a new class here,
 * not editing PaymentService.
 *
 * LSP reminder (Lab 2 reflection): every implementation must honor the same
 * contract - a `true` return must mean the charge genuinely succeeded.
 * A strategy that returns true without actually charging anything (e.g. a
 * gift card with insufficient balance) breaks that contract for every caller.
 */
public interface PaymentStrategy {
    boolean charge(double amount);
}
