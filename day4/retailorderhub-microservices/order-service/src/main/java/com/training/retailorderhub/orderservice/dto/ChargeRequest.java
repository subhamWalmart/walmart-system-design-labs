package com.training.retailorderhub.orderservice.dto;

/** Mirrors payment-service's dto/ChargeRequest.java - see PaymentClient. */
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
