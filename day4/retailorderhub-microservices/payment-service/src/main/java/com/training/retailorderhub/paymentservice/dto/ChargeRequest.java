package com.training.retailorderhub.paymentservice.dto;

/**
 * TRAINING NOTE: each service defines its own small DTOs rather than sharing
 * a common library module. For a five-service training repo that keeps every
 * service buildable and readable on its own - no shared jar to publish or
 * version. In a larger real system you'd more likely generate these from a
 * shared OpenAPI contract, or use a schema registry for async messages, so
 * order-service and payment-service can't quietly drift out of sync.
 */
public class ChargeRequest {

    private String paymentMethod;
    private double amount;

    public ChargeRequest() {
    }

    public ChargeRequest(String paymentMethod, double amount) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
