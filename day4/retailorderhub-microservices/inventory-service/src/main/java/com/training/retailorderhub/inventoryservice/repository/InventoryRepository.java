package com.training.retailorderhub.inventoryservice.repository;

/**
 * Carried forward unchanged from the Day 2 monolith (DIP: InventoryService
 * depends on this abstraction, not on a concrete data-access class).
 * JpaInventoryRepository is the only implementation, here as before.
 */
public interface InventoryRepository {

    int getQuantity(String itemName);

    void decrementQuantity(String itemName);
}
