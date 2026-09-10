package com.training.retailorderhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Read-only view of the "product" table for the Catalog page.
 *
 * NOTE: this entity exists only to list products on the UI. Inventory checks
 * and decrements still go through InventoryRepository / JpaInventoryRepository
 * (Day 2, DIP) - that native-query path is untouched, including its
 * intentional SQL-injection finding. This entity does not replace it.
 */
@Entity
@Table(name = "product")
public class Product {

    @Id
    private Long id;

    private String name;

    private BigDecimal price;

    private int quantity;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
