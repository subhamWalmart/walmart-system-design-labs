package com.training.retailorderhub.inventoryservice.service;

import com.training.retailorderhub.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carried forward from the Day 2 monolith, with one fix this decomposition
 * needed that the monolith never had to think about:
 *
 * TRAINING NOTE (bug found running this service standalone):
 * JpaInventoryRepository.decrementQuantity() runs a native UPDATE via
 * entityManager.createNativeQuery(...).executeUpdate() - JPA requires an
 * active transaction for any write, or it throws
 * jakarta.persistence.TransactionRequiredException. In the monolith this
 * worked by accident: it ran inside OrderService.processOrder(), which WAS
 * @Transactional, and that transaction propagated across the in-process
 * method calls into this class. Now that inventory-service is its own
 * process, that borrowed transaction is gone - nothing about calling this
 * service over HTTP creates one. getQuantity() (a plain SELECT) never
 * needed a transaction, which is why the stock check earlier in the same
 * order succeeds and only the decrement fails.
 *
 * The fix is the @Transactional below - it makes this method itself the
 * transaction boundary, the same role OrderService's method used to play by
 * coincidence. Decomposing a monolith doesn't just move code: it can strip
 * away transactional context that calling code was silently relying on,
 * and each service has to establish its own instead.
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

    public int getQuantity(String itemName) {
        return inventoryRepository.getQuantity(itemName);
    }

    @Transactional
    public void decrementQuantity(String itemName) {
        inventoryRepository.decrementQuantity(itemName);
    }
}
