package com.training.retailorderhub.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * TRAINING NOTE (Day 2, Lab 1, Step 4):
 * This is the JPA-backed implementation of InventoryRepository, extracted
 * from the original OrderManager.getInventoryQuantity() and the "Update
 * inventory" loop.
 *
 * DELIBERATELY NOT FIXED HERE: the native queries below are still built by
 * string concatenation, which is the same SQL injection finding from Day 1's
 * Lab 3. Today's refactor is a SOLID exercise (SRP, OCP, DIP) - it is not a
 * security pass, and this class carries the vulnerability forward on purpose.
 * If you're using this as an answer key, do not "helpfully" fix this without
 * flagging it - the vulnerability is intentional so the finding stays valid
 * for later discussion.
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
