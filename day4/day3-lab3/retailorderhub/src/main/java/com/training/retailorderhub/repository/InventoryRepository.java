package com.training.retailorderhub.repository;

/**
 * TRAINING NOTE (Day 2, Lab 1, Step 4 - Dependency Inversion Principle):
 * InventoryService depends on this abstraction, not on a concrete data-access
 * class. JpaInventoryRepository is the only implementation today, but a
 * different implementation (e.g. a CachedInventoryRepository backed by Redis)
 * could be swapped in without InventoryService ever changing.
 *
 * See Lab 1's LSP reflection question: any new implementation must honor the
 * same contract - in particular, a call to decrementQuantity() must be
 * reflected by the very next call to getQuantity(), or callers relying on
 * isInStock() can be silently misled.
 */
public interface InventoryRepository {

    int getQuantity(String itemName);

    void decrementQuantity(String itemName);
}
