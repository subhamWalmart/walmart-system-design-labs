package com.training.retailorderhub.orderservice.dto;

/**
 * Mirrors the JSON shape returned by
 * GET /api/inventory/{itemName}/quantity on inventory-service. See the
 * TRAINING NOTE on payment-service's ChargeRequest for why each service
 * keeps its own small copy of these DTOs instead of sharing one module.
 */
public class InventoryQuantityResponse {

    private String itemName;
    private int quantity;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
