package com.training.retailorderhub.service;

import com.training.retailorderhub.repository.InventoryRepository;
import org.springframework.stereotype.Service;

/**
 * TRAINING NOTE (Day 2, Lab 1):
 * Extracted from OrderManager (SRP, Step 2), then updated to depend on
 * InventoryRepository instead of EntityManager directly (DIP, Step 4).
 * This class has no EntityManager import at all.
 */
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public boolean isInStock(String itemName) {
        return inventoryRepository.getQuantity(itemName) > 0;
    }

    public void decrementQuantity(String itemName) {
        inventoryRepository.decrementQuantity(itemName);
    }
}
