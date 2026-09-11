package com.training.retailorderhub.paymentservice.dto;

public class ChargeResponse {

    private boolean success;

    public ChargeResponse() {
    }

    public ChargeResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
