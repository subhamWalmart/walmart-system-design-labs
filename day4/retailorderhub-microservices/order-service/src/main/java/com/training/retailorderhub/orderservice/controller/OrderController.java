package com.training.retailorderhub.orderservice.controller;

import com.training.retailorderhub.orderservice.dto.OrderRequest;
import com.training.retailorderhub.orderservice.dto.OrderResponse;
import com.training.retailorderhub.orderservice.model.Order;
import com.training.retailorderhub.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TRAINING NOTE (Day 4 - Decomposition Pattern):
 * The monolith's OrderController was a Thymeleaf @Controller: "/" rendered
 * the catalog (via ProductRepository) and order form, "/order" handled the
 * form POST, "/orders" rendered a list page. All three needed data this
 * service no longer owns - see the root README for why the UI was dropped
 * rather than carried forward. What's left is the part that was always
 * OrderService's own job: place an order, list orders.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        Order order = orderService.processOrder(
                request.getCustomerId(),
                request.getItemNames(),
                request.getPaymentMethod(),
                request.getAmount());

        if (order == null) {
            return new OrderResponse(false, null,
                    "Order failed. Check inventory, payment method, or required fields.");
        }
        return new OrderResponse(true, order.getId(), "Order placed successfully.");
    }

    @GetMapping
    public List<Order> listOrders() {
        return orderService.listOrders();
    }
}
