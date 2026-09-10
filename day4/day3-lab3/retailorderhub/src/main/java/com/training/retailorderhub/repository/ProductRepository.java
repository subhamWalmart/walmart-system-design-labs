package com.training.retailorderhub.repository;

import com.training.retailorderhub.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Read-only catalog listing for the UI (findAll(), ordered by name in the
 * controller). Stock checks and decrements for placing an order still go
 * through InventoryRepository, not this interface.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
