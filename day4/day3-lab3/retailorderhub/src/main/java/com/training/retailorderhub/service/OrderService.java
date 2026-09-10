package com.training.retailorderhub.service;

import com.training.retailorderhub.model.Order;
import com.training.retailorderhub.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TRAINING NOTE (Day 2, Lab 1):
 * This class is what's left of the Day 1 "OrderManager" God Object after the
 * SRP -> DIP refactor:
 *   - Step 1: PaymentService extracted (payment logic).
 *   - Step 2: InventoryService extracted (stock checks + updates).
 *   - Step 3: OrderManager renamed to OrderService; validateCustomer() and
 *     validateItems() deleted - they duplicated the inline checks below and
 *     were never actually called (Day 1 Lab 3's Duplicated Code finding).
 *   - Step 4: DIP applied to InventoryService (not this class directly) -
 *     see InventoryRepository.
 *
 * What's left here is pure orchestration: validate input, check stock,
 * charge, persist, update stock. That orchestration is OrderService's one
 * job.
 *
 * STILL OPEN (by design - not fixed by this refactor): the customerId/
 * itemNames null checks below are the same inline validation from Day 1;
 * TOCTOU race condition between the stock check and the stock decrement
 * (Day 1 Lab 3 findings) is a structural/concurrency issue, not a SOLID
 * violation, and is intentionally left as a valid finding against this code.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    public OrderService(OrderRepository orderRepository,
                         InventoryService inventoryService,
                         PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
    }

    @Transactional
    public boolean processOrder(String customerId, List<String> itemNames,
                                 String paymentMethod, double amount) {
        if (customerId == null || customerId.isEmpty()) {
            return false;
        }
        if (itemNames == null || itemNames.isEmpty()) {
            return false;
        }

        for (String itemName : itemNames) {
            if (!inventoryService.isInStock(itemName)) {
                return false;
            }
        }

        if (!paymentService.charge(paymentMethod, amount)) {
            return false;
        }

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setItemNames(String.join(",", itemNames));
        order.setPaymentMethod(paymentMethod);
        order.setAmount(amount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);

        for (String itemName : itemNames) {
            inventoryService.decrementQuantity(itemName);
        }

        return true;
    }
}
