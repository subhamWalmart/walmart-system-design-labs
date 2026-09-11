package com.training.retailorderhub.orderservice.dto;

import java.util.List;

/**
 * Request body for POST /api/orders. Replaces the monolith's HTML form post
 * (customerId, items, paymentMethod, amount as request params) with a JSON
 * body - this service is API-only now, see the root README for why the
 * Thymeleaf UI was dropped rather than carried forward.
 */
public class OrderRequest {

    private String customerId;
    private List<String> itemNames;
    private String paymentMethod;
    private double amount;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<String> getItemNames() {
        return itemNames;
    }

    public void setItemNames(List<String> itemNames) {
        this.itemNames = itemNames;
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
