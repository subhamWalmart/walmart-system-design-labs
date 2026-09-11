package com.training.retailorderhub.inventoryservice.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * TRAINING NOTE - carried forward from the Day 2 monolith on purpose:
 * both native queries below are still built by string concatenation, the
 * same SQL injection finding from Day 1's Lab 3. Moving this class into its
 * own service does not fix it - if anything, exposing getQuantity()/
 * decrementQuantity() over a REST endpoint (see InventoryController) makes
 * the itemName parameter reachable from further away than it was in the
 * monolith. That's worth calling out explicitly if you're using this repo as
 * a follow-on discussion prompt: decomposition and security are orthogonal -
 * splitting a monolith into services doesn't fix vulnerabilities that were
 * already there, and can widen their blast radius if the new network
 * boundary isn't paired with the same input validation you'd want anywhere
 * else.
 */
@Repository
public class JpaInventoryRepository implements InventoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int getQuantity(String itemName) {
        String query = "SELECT quantity FROM product WHERE name = '" + itemName + "'";
        try {
            Object result = entityManager.createNativeQuery(query).getSingleResult();
            return ((Number) result).intValue();
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public void decrementQuantity(String itemName) {
        String updateQuery = "UPDATE product SET quantity = quantity - 1 WHERE name = '" + itemName + "'";
        entityManager.createNativeQuery(updateQuery).executeUpdate();
    }
}
