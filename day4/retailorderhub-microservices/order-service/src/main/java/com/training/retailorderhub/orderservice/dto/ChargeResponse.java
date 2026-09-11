package com.training.retailorderhub.orderservice.dto;

/** Mirrors payment-service's dto/ChargeResponse.java - see PaymentClient. */
public class ChargeResponse {

    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
