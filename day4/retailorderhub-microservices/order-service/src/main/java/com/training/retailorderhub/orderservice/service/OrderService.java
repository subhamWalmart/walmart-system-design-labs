package com.training.retailorderhub.orderservice.service;

import com.training.retailorderhub.orderservice.client.InventoryClient;
import com.training.retailorderhub.orderservice.client.PaymentClient;
import com.training.retailorderhub.orderservice.dto.ChargeRequest;
import com.training.retailorderhub.orderservice.dto.ChargeResponse;
import com.training.retailorderhub.orderservice.model.Order;
import com.training.retailorderhub.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TRAINING NOTE (Day 4 - Decomposition Pattern, carried forward from Day 2):
 * Same orchestration as the monolith's OrderService - validate, check stock,
 * charge, persist, update stock - and the same open findings on purpose:
 * the customerId/itemNames null checks are still the plain inline checks
 * from Day 1, and stock is still checked in one loop and decremented in a
 * separate later loop, with payment in between.
 *
 * That TOCTOU race is sharper now than it was in the monolith. In-process,
 * the whole method ran inside one @Transactional boundary against one
 * database. Now the stock check is a network call to inventory-service, the
 * charge is a network call to payment-service, and the decrement is another
 * network call to inventory-service - three separate operations against two
 * other services' own databases, and @Transactional here only covers this
 * service's own orderRepository.save(). If payment-service or inventory-
 * service is momentarily unreachable partway through, or if two orders for
 * the last unit of the same item race each other across that gap, nothing
 * rolls anything back. This is exactly the problem the Day 4 Saga Pattern
 * slide describes (compensating transactions, choreography vs.
 * orchestration) - fixing it properly is the natural next lab, not something
 * folded in here silently.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    public OrderService(OrderRepository orderRepository,
                         InventoryClient inventoryClient,
                         PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public Order processOrder(String customerId, List<String> itemNames,
                               String paymentMethod, double amount) {
        if (customerId == null || customerId.isEmpty()) {
            return null;
        }
        if (itemNames == null || itemNames.isEmpty()) {
            return null;
        }

        for (String itemName : itemNames) {
            int quantity = inventoryClient.getQuantity(itemName).getQuantity();
            if (quantity <= 0) {
                return null;
            }
        }

        ChargeResponse chargeResponse = paymentClient.charge(new ChargeRequest(paymentMethod, amount));
        if (chargeResponse == null || !chargeResponse.isSuccess()) {
            return null;
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
            inventoryClient.decrement(itemName);
        }

        return order;
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }
}
