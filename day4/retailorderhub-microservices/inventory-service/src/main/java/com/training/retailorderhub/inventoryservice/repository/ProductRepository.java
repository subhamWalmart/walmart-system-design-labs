package com.training.retailorderhub.inventoryservice.repository;

import com.training.retailorderhub.inventoryservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
