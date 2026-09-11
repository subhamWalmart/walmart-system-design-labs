package com.training.retailorderhub.orderservice.repository;

import com.training.retailorderhub.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // findById(), save(), delete() etc. come from JpaRepository.
    //
    // TRAINING NOTE (carried forward from Day 2's ISP reflection):
    // Keep this interface limited to data access. If a reporting or accounting
    // capability is ever needed, it belongs on its own interface, so clients
    // which only need findById()/save() aren't forced to depend on methods
    // they never call.
}
